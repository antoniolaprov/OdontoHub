package com.g4.odontohub.confirmacao.naocomparecimento.domain.model;

import java.time.LocalDate;

/**
 * F17 — Registro de Não Comparecimento (No-Show).
 * Registra a ausência do paciente em um agendamento, diferenciando faltas
 * justificadas de injustificadas (estas alimentam a análise de churn — F11).
 */
public class NaoComparecimento {

    private final NaoComparecimentoId id;
    private final Long agendamentoId;
    private final String paciente;
    private final LocalDate dataConsulta;
    private final boolean justificada;
    private final String justificativa;
    private LocalDate dataRegistro;

    public NaoComparecimento(NaoComparecimentoId id, Long agendamentoId, String paciente,
                             LocalDate dataConsulta, boolean justificada, String justificativa) {
        this.id = id;
        this.agendamentoId = agendamentoId;
        this.paciente = paciente;
        this.dataConsulta = dataConsulta;
        this.justificada = justificada;
        this.justificativa = justificativa;
        this.dataRegistro = LocalDate.now();
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static NaoComparecimento reconstituir(NaoComparecimentoId id, Long agendamentoId, String paciente,
                                                 LocalDate dataConsulta, boolean justificada, String justificativa,
                                                 LocalDate dataRegistro) {
        NaoComparecimento r = new NaoComparecimento(id, agendamentoId, paciente, dataConsulta, justificada, justificativa);
        r.dataRegistro = dataRegistro;
        return r;
    }

    public boolean contaParaReincidencia() {
        return !justificada;
    }

    public NaoComparecimentoId getId() { return id; }
    public Long getAgendamentoId() { return agendamentoId; }
    public String getPaciente() { return paciente; }
    public LocalDate getDataConsulta() { return dataConsulta; }
    public boolean isJustificada() { return justificada; }
    public String getJustificativa() { return justificativa; }
    public LocalDate getDataRegistro() { return dataRegistro; }
}
