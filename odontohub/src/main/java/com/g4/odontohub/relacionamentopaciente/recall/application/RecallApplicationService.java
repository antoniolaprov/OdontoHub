package com.g4.odontohub.relacionamentopaciente.recall.application;

import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallCanceladoPorAgendamentoExistente;
import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallEscalonado;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.*;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;
import com.g4.odontohub.relacionamentopaciente.recall.domain.service.RecallService;
import com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.InMemoryRecallRepository;
import com.g4.odontohub.relacionamentopaciente.followup.application.FollowupApplicationService;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.List;

public class RecallApplicationService {

    /** Tipo de tarefa de follow-up criada quando um recall é escalonado (F07 → F10). */
    private static final String TIPO_TAREFA_ESCALONAMENTO = "ESCALONAMENTO_RECALL";

    private final RecallService service;

    public RecallApplicationService() {
        this(new InMemoryRecallRepository());
    }

    public RecallApplicationService(RecallRepository repositorio) {
        this.service = new RecallService(repositorio);
    }

    /**
     * Composition root de produção: integra o escalonamento de recall (F07) com o
     * follow-up (F10). Cada {@link RecallEscalonado} gera uma tarefa para que um
     * responsável retome o contato com o paciente.
     */
    public RecallApplicationService(RecallRepository repositorio,
                                    FollowupApplicationService followupService) {
        this.service = new RecallService(repositorio);
        DomainEventPublisher.subscribe(RecallEscalonado.class, evento ->
                followupService.criarTarefa(evento.paciente(), TIPO_TAREFA_ESCALONAMENTO));
    }

    public Recall processarGatilhoRecall(String paciente, String procedimento) {
        return service.dispararRecall(paciente, procedimento);
    }

    public Recall inserirNaFila(String paciente) {
        return service.inserirNaFila(paciente);
    }

    public void registrarAgendamentoFuturo(String paciente, Long agendamentoId) {
        service.registrarAgendamentoFuturo(paciente, agendamentoId);
    }

    public void verificarSobreposicao(String paciente) {
        service.verificarECancelarSeJaAgendado(paciente);
    }

    public Recall registrarConversao(String paciente, Long agendamentoId) {
        return service.registrarConversao(paciente, agendamentoId);
    }

    public boolean estaNaFila(String paciente) {
        return service.estaNaFila(paciente);
    }

    public Recall buscarPorPaciente(String paciente) {
        return service.buscarPorPaciente(paciente);
    }

    public RecallCanceladoPorAgendamentoExistente getUltimoCancelamento() {
        return service.getUltimoCancelamento();
    }

    public List<Recall> todos() {
        return service.todos();
    }

    public List<Recall> filaPriorizada() {
        return service.filaPriorizada();
    }

    public NivelPrioridade prioridadeDe(String paciente) {
        return service.prioridadeDe(paciente);
    }

    public boolean registrarTentativaContato(String paciente) {
        return service.registrarTentativaContato(paciente);
    }

    public RecallEscalonado getUltimoEscalonamento() {
        return service.getUltimoEscalonamento();
    }

    public double taxaConversao() {
        return service.taxaConversao();
    }

    public Recall definirFatores(String paciente, FatoresPriorizacao fatores) {
        return service.definirFatores(paciente, fatores);
    }

    public boolean registrarTentativaDetalhada(String paciente, CanalContato canal, ResultadoContato resultado,
                                               String responsavel, int duracaoMinutos, String observacoes) {
        return service.registrarTentativaDetalhada(paciente, canal, resultado, responsavel, duracaoMinutos, observacoes);
    }

    public Recall excluirAutomaticamente(String paciente, MotivoExclusao motivo) {
        return service.excluirAutomaticamente(paciente, motivo);
    }

    public List<Recall> filaComSlaVencido(LocalDate hoje) {
        return service.filaComSlaVencido(hoje);
    }

    public CategoriaRecall categoriaDe(String paciente) {
        return service.categoriaDe(paciente);
    }

    public IndicadorVisual indicadorDe(String paciente) {
        return service.indicadorDe(paciente);
    }

    public int pontuacaoDe(String paciente) {
        return service.pontuacaoDe(paciente);
    }

    public long pacientesRecuperados() {
        return service.pacientesRecuperados();
    }

    public long pacientesPerdidos() {
        return service.pacientesPerdidos();
    }
}
