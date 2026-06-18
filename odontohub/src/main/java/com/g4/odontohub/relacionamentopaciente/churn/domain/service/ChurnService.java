package com.g4.odontohub.relacionamentopaciente.churn.domain.service;

import com.g4.odontohub.relacionamentopaciente.churn.domain.event.PacienteClassificadoComoChurn;
import com.g4.odontohub.relacionamentopaciente.churn.domain.event.PacienteEntrandoZonaDeRisco;
import com.g4.odontohub.relacionamentopaciente.churn.domain.event.PacienteReativado;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.*;
import com.g4.odontohub.relacionamentopaciente.churn.domain.repository.ChurnRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChurnService {

    private static final int MESES_EVASAO = 12;
    private static final int MESES_ZONA_RISCO = 6;

    private final ChurnRepository repositorio;
    private final Set<String> alertasInatividade = new HashSet<>();
    /** Contagem de cancelamentos por categoria de motivo, base do gráfico de Pareto. */
    private final Map<String, Integer> motivosCancelamento = new LinkedHashMap<>();

    private double valorMedioHora;
    private String ultimoRegistroAgendamento;
    private int noShowCount;
    private int cancelamentoCount;

    public ChurnService(ChurnRepository repositorio) {
        this.repositorio = repositorio;
    }

    private AnaliseChurn obterOuCriar(String paciente) {
        AnaliseChurn analise = repositorio.buscarPorPaciente(paciente);
        if (analise == null) {
            analise = new AnaliseChurn(new ChurnId(repositorio.proximoId()), paciente);
            repositorio.salvar(analise);
        }
        return analise;
    }

    public void definirValorMedioHora(double valorMedioHora) {
        this.valorMedioHora = valorMedioHora;
    }

    public void definirSemAgendamentoFuturo(String paciente) {
        AnaliseChurn a = obterOuCriar(paciente);
        a.setPossuiAgendamentoFuturo(false);
        repositorio.salvar(a);
    }

    public void definirMesesSemRetorno(String paciente, int meses) {
        AnaliseChurn a = obterOuCriar(paciente);
        a.setMesesSemRetorno(meses);
        repositorio.salvar(a);
    }

    public void definirPlanoAtivo(String paciente, boolean ativo) {
        AnaliseChurn a = obterOuCriar(paciente);
        a.setPossuiPlanoAtivo(ativo);
        repositorio.salvar(a);
    }

    public void recalcularStatusChurn() {
        for (AnaliseChurn analise : repositorio.todos()) {
            boolean semRetornoProlongado = analise.getMesesSemRetorno() >= MESES_EVASAO;
            boolean naZonaDeRisco = analise.getMesesSemRetorno() >= MESES_ZONA_RISCO
                    && analise.getMesesSemRetorno() < MESES_EVASAO;

            if (!analise.isPossuiAgendamentoFuturo() && semRetornoProlongado && analise.isPossuiPlanoAtivo()) {
                analise.classificar(StatusChurn.EVADIDO);
                repositorio.salvar(analise);
                DomainEventPublisher.publish(new PacienteClassificadoComoChurn(analise.getId(), analise.getPaciente()));
            } else if (naZonaDeRisco && analise.isPossuiPlanoAtivo()) {
                analise.classificar(StatusChurn.ZONA_DE_RISCO);
                repositorio.salvar(analise);
                alertasInatividade.add(analise.getPaciente());
                DomainEventPublisher.publish(new PacienteEntrandoZonaDeRisco(
                        analise.getId(), analise.getPaciente(), analise.getMesesSemRetorno()));
            }
        }
    }

    public void classificarManualmente(String paciente, StatusChurn status) {
        AnaliseChurn a = obterOuCriar(paciente);
        a.classificar(status);
        repositorio.salvar(a);
    }

    public void reativar(String paciente) {
        AnaliseChurn analise = obterOuCriar(paciente);
        analise.classificar(StatusChurn.REATIVADO);
        repositorio.salvar(analise);
        DomainEventPublisher.publish(new PacienteReativado(analise.getId(), analise.getPaciente()));
    }

    public StatusChurn statusDe(String paciente) {
        return obterOuCriar(paciente).getStatusChurn();
    }

    public boolean temAlertaInatividade(String paciente) {
        return alertasInatividade.contains(paciente);
    }

    public double calcularReceitaPerdida(int horasDeAgendaOciosa) {
        return horasDeAgendaOciosa * valorMedioHora;
    }

    public void registrarNoShow(String paciente) {
        this.ultimoRegistroAgendamento = "Não Compareceu";
        this.noShowCount++;
    }

    public void cancelarAgendamento(String paciente, String categoriaMotivo) {
        if (categoriaMotivo == null || categoriaMotivo.isBlank()) {
            throw new IllegalArgumentException("A categoria do motivo de cancelamento é obrigatória");
        }
        this.cancelamentoCount++;
        motivosCancelamento.merge(categoriaMotivo, 1, Integer::sum);
    }

    /**
     * Gráfico de Pareto dos motivos de cancelamento (F11): motivos ordenados do mais
     * frequente ao menos frequente, com percentual individual e acumulado.
     */
    public List<ItemPareto> paretoMotivosCancelamento() {
        int total = motivosCancelamento.values().stream().mapToInt(Integer::intValue).sum();
        List<ItemPareto> pareto = new ArrayList<>();
        if (total == 0) {
            return pareto;
        }
        List<Map.Entry<String, Integer>> ordenados = new ArrayList<>(motivosCancelamento.entrySet());
        ordenados.sort(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed());
        double acumulado = 0.0;
        for (Map.Entry<String, Integer> entrada : ordenados) {
            double percentual = (double) entrada.getValue() / total;
            acumulado += percentual;
            pareto.add(new ItemPareto(entrada.getKey(), entrada.getValue(), percentual, acumulado));
        }
        return pareto;
    }

    public void definirTipoProcedimento(String paciente, String tipoProcedimento) {
        AnaliseChurn a = obterOuCriar(paciente);
        a.setTipoProcedimento(tipoProcedimento);
        repositorio.salvar(a);
    }

    /** Filtro de churn por tipo de procedimento: quantos pacientes evadidos têm aquele tratamento. */
    public long contarChurnPorProcedimento(String tipoProcedimento) {
        return repositorio.todos().stream()
                .filter(a -> a.getStatusChurn() == StatusChurn.EVADIDO)
                .filter(a -> tipoProcedimento != null && tipoProcedimento.equalsIgnoreCase(a.getTipoProcedimento()))
                .count();
    }

    public String getUltimoRegistroAgendamento() { return ultimoRegistroAgendamento; }
    public int getNoShowCount() { return noShowCount; }
    public int getCancelamentoCount() { return cancelamentoCount; }
}
