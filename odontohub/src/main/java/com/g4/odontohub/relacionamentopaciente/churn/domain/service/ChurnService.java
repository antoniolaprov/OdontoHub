package com.g4.odontohub.relacionamentopaciente.churn.domain.service;

import com.g4.odontohub.relacionamentopaciente.churn.domain.event.PacienteClassificadoComoChurn;
import com.g4.odontohub.relacionamentopaciente.churn.domain.event.PacienteEntrandoZonaDeRisco;
import com.g4.odontohub.relacionamentopaciente.churn.domain.event.PacienteReativado;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.AnaliseChurn;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.ChurnId;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.StatusChurn;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChurnService {

    private static final int MESES_EVASAO = 12;
    private static final int MESES_ZONA_RISCO = 6;

    private final Map<String, AnaliseChurn> analisesPorPaciente = new HashMap<>();
    private final Set<String> alertasInatividade = new HashSet<>();
    private long nextId = 1;

    private double valorMedioHora;
    private String ultimoRegistroAgendamento;
    private int noShowCount;
    private int cancelamentoCount;

    public AnaliseChurn dados(String paciente) {
        return analisesPorPaciente.computeIfAbsent(paciente,
                p -> new AnaliseChurn(new ChurnId(nextId++), p));
    }

    public void definirValorMedioHora(double valorMedioHora) {
        this.valorMedioHora = valorMedioHora;
    }

    public void recalcularStatusChurn() {
        for (AnaliseChurn analise : analisesPorPaciente.values()) {
            boolean semRetornoProlongado = analise.getMesesSemRetorno() >= MESES_EVASAO;
            boolean naZonaDeRisco = analise.getMesesSemRetorno() >= MESES_ZONA_RISCO
                    && analise.getMesesSemRetorno() < MESES_EVASAO;

            if (!analise.isPossuiAgendamentoFuturo() && semRetornoProlongado && analise.isPossuiPlanoAtivo()) {
                analise.classificar(StatusChurn.EVADIDO);
                DomainEventPublisher.publish(new PacienteClassificadoComoChurn(analise.getId(), analise.getPaciente()));
            } else if (naZonaDeRisco && analise.isPossuiPlanoAtivo()) {
                analise.classificar(StatusChurn.ZONA_DE_RISCO);
                alertasInatividade.add(analise.getPaciente());
                DomainEventPublisher.publish(new PacienteEntrandoZonaDeRisco(
                        analise.getId(), analise.getPaciente(), analise.getMesesSemRetorno()));
            }
        }
    }

    public void classificarManualmente(String paciente, StatusChurn status) {
        dados(paciente).classificar(status);
    }

    public void reativar(String paciente) {
        AnaliseChurn analise = dados(paciente);
        analise.classificar(StatusChurn.REATIVADO);
        DomainEventPublisher.publish(new PacienteReativado(analise.getId(), analise.getPaciente()));
    }

    public StatusChurn statusDe(String paciente) {
        return dados(paciente).getStatusChurn();
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
    }

    public String getUltimoRegistroAgendamento() { return ultimoRegistroAgendamento; }
    public int getNoShowCount() { return noShowCount; }
    public int getCancelamentoCount() { return cancelamentoCount; }
}
