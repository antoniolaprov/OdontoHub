package com.g4.odontohub.relacionamentopaciente.recall.domain.service;

import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallCanceladoPorAgendamentoExistente;
import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallConvertido;
import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallDisparado;
import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallEscalonado;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.*;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RecallService {

    private final RecallRepository repositorio;
    private final Map<String, Long> agendamentoFuturoPorPaciente = new HashMap<>();
    private RecallCanceladoPorAgendamentoExistente ultimoCancelamento;
    private RecallEscalonado ultimoEscalonamento;

    public RecallService(RecallRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Gatilhos temporais dinâmicos por tipo de procedimento (configuráveis). */
    private int prazoParaRetorno(String procedimento) {
        return switch (procedimento == null ? "" : procedimento.trim().toUpperCase()) {
            case "PROFILAXIA" -> 180;
            case "IMPLANTE" -> 45;
            case "CIRURGIA" -> 1;
            case "ORTODONTIA" -> 30;
            default -> 90;
        };
    }

    public Recall dispararRecall(String paciente, String procedimentoGatilho) {
        int dias = prazoParaRetorno(procedimentoGatilho);
        RecallId id = new RecallId(repositorio.proximoId());
        Recall recall = new Recall(id, paciente, procedimentoGatilho, dias);
        repositorio.salvar(recall);
        DomainEventPublisher.publish(new RecallDisparado(id, paciente, procedimentoGatilho, dias));
        return recall;
    }

    public Recall inserirNaFila(String paciente) {
        return dispararRecall(paciente, "Profilaxia");
    }

    public void registrarAgendamentoFuturo(String paciente, Long agendamentoId) {
        agendamentoFuturoPorPaciente.put(paciente, agendamentoId);
    }

    public void verificarECancelarSeJaAgendado(String paciente) {
        Long agendamentoId = agendamentoFuturoPorPaciente.get(paciente);
        if (agendamentoId == null) {
            return;
        }
        recallNaFila(paciente).ifPresent(recall -> {
            recall.cancelar();
            repositorio.salvar(recall);
            ultimoCancelamento = new RecallCanceladoPorAgendamentoExistente(recall.getId(), agendamentoId);
            DomainEventPublisher.publish(ultimoCancelamento);
        });
    }

    public Recall registrarConversao(String paciente, Long agendamentoGeradoId) {
        Recall recall = recallNaFila(paciente)
                .orElseThrow(() -> new IllegalStateException("Paciente não está na fila de recall: " + paciente));
        recall.converter();
        repositorio.salvar(recall);
        DomainEventPublisher.publish(new RecallConvertido(recall.getId(), agendamentoGeradoId));
        return recall;
    }

    public boolean estaNaFila(String paciente) {
        return recallNaFila(paciente).isPresent();
    }

    /** Lista todos os recalls (camada de leitura para o frontend). */
    public List<Recall> todos() {
        return repositorio.todos();
    }

    public Recall buscarPorPaciente(String paciente) {
        return repositorio.todos().stream()
                .filter(r -> r.getPaciente().equals(paciente))
                .reduce((primeiro, segundo) -> segundo)
                .orElseThrow(() -> new IllegalArgumentException("Recall não encontrado para: " + paciente));
    }

    public RecallCanceladoPorAgendamentoExistente getUltimoCancelamento() {
        return ultimoCancelamento;
    }

    /**
     * Fila de recall ordenada pela pontuação do motor de priorização inteligente
     * (maior pontuação primeiro; desempate pelo menor prazo de retorno).
     */
    public List<Recall> filaPriorizada() {
        return repositorio.todos().stream()
                .filter(r -> r.getStatus() == StatusRecall.NA_FILA)
                .sorted(Comparator.comparingInt(Recall::pontuacaoPrioridade).reversed()
                        .thenComparingInt(Recall::getDiasParaRetorno))
                .toList();
    }

    /** Define os fatores de priorização do recall na fila e reclassifica a categoria. */
    public Recall definirFatores(String paciente, FatoresPriorizacao fatores) {
        Recall recall = recallNaFila(paciente)
                .orElseThrow(() -> new IllegalStateException("Paciente não está na fila de recall: " + paciente));
        recall.definirFatores(fatores);
        repositorio.salvar(recall);
        return recall;
    }

    /** Registra uma tentativa de contato detalhada; escalona o caso ao atingir o limite. */
    public boolean registrarTentativaDetalhada(String paciente, CanalContato canal, ResultadoContato resultado,
                                                String responsavel, int duracaoMinutos, String observacoes) {
        Recall recall = recallNaFila(paciente)
                .orElseThrow(() -> new IllegalStateException("Paciente não está na fila de recall: " + paciente));
        TentativaContato tentativa = new TentativaContato(
                responsavel, LocalDateTime.now(), canal, duracaoMinutos, resultado, observacoes);
        boolean escalonar = recall.registrarTentativa(tentativa);
        repositorio.salvar(recall);
        if (escalonar) {
            ultimoEscalonamento = new RecallEscalonado(recall.getId(), paciente, recall.getTentativasContato());
            DomainEventPublisher.publish(ultimoEscalonamento);
        }
        return escalonar;
    }

    /** Exclui o paciente do recall automaticamente (falecido, transferido, alta, judicial). */
    public Recall excluirAutomaticamente(String paciente, MotivoExclusao motivo) {
        Recall recall = repositorio.todos().stream()
                .filter(r -> r.getPaciente().equals(paciente))
                .filter(r -> r.getStatus() == StatusRecall.NA_FILA || r.getStatus() == StatusRecall.AGENDADO)
                .reduce((a, b) -> b)
                .orElseThrow(() -> new IllegalStateException("Sem recall ativo para: " + paciente));
        recall.excluir(motivo);
        repositorio.salvar(recall);
        return recall;
    }

    /** Casos críticos na fila cujo SLA de contato foi ultrapassado (F07). */
    public List<Recall> filaComSlaVencido(LocalDate hoje) {
        return repositorio.todos().stream()
                .filter(r -> r.slaVencido(hoje))
                .toList();
    }

    public CategoriaRecall categoriaDe(String paciente) {
        return buscarPorPaciente(paciente).getCategoria();
    }

    public IndicadorVisual indicadorDe(String paciente) {
        return buscarPorPaciente(paciente).indicadorVisual();
    }

    public int pontuacaoDe(String paciente) {
        return buscarPorPaciente(paciente).pontuacaoPrioridade();
    }

    /** Pacientes recuperados = recalls convertidos em agendamento (métrica F07). */
    public long pacientesRecuperados() {
        return repositorio.todos().stream()
                .filter(r -> r.getStatus() == StatusRecall.CONVERTIDO)
                .count();
    }

    /** Pacientes perdidos = recalls excluídos automaticamente (métrica F07). */
    public long pacientesPerdidos() {
        return repositorio.todos().stream()
                .filter(r -> r.getStatus() == StatusRecall.EXCLUIDO)
                .count();
    }

    public NivelPrioridade prioridadeDe(String paciente) {
        return buscarPorPaciente(paciente).getPrioridade();
    }

    /**
     * Registra uma tentativa de contato (SLA). Ao atingir o limite, escalona o caso
     * e publica {@link RecallEscalonado}.
     */
    public boolean registrarTentativaContato(String paciente) {
        Recall recall = recallNaFila(paciente)
                .orElseThrow(() -> new IllegalStateException("Paciente não está na fila de recall: " + paciente));
        boolean escalonar = recall.registrarTentativaContato();
        repositorio.salvar(recall);
        if (escalonar) {
            ultimoEscalonamento = new RecallEscalonado(recall.getId(), paciente, recall.getTentativasContato());
            DomainEventPublisher.publish(ultimoEscalonamento);
        }
        return escalonar;
    }

    public RecallEscalonado getUltimoEscalonamento() {
        return ultimoEscalonamento;
    }

    /** Taxa de conversão = recalls convertidos / total de recalls disparados (métrica F07). */
    public double taxaConversao() {
        long total = repositorio.todos().size();
        if (total == 0) {
            return 0.0;
        }
        long convertidos = repositorio.todos().stream()
                .filter(r -> r.getStatus() == StatusRecall.CONVERTIDO)
                .count();
        return (double) convertidos / total;
    }

    private Optional<Recall> recallNaFila(String paciente) {
        return repositorio.todos().stream()
                .filter(r -> r.getPaciente().equals(paciente))
                .filter(r -> r.getStatus() == StatusRecall.NA_FILA)
                .findFirst();
    }
}
