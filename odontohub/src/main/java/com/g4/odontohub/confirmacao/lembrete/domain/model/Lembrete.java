package com.g4.odontohub.confirmacao.lembrete.domain.model;

import java.time.LocalDate;

/**
 * F16 — Confirmação e Lembretes de Consulta.
 * Representa um lembrete vinculado a um agendamento, com ciclo de vida
 * Pendente → Enviado → Confirmado/Recusado/Sem Resposta.
 */
public class Lembrete {

    private final LembreteId id;
    private final Long agendamentoId;
    private final String paciente;
    private final CanalLembrete canal;
    private StatusLembrete status;
    private LocalDate dataEnvio;
    private String motivoRecusa;

    public Lembrete(LembreteId id, Long agendamentoId, String paciente, CanalLembrete canal) {
        this.id = id;
        this.agendamentoId = agendamentoId;
        this.paciente = paciente;
        this.canal = canal;
        this.status = StatusLembrete.PENDENTE;
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Lembrete reconstituir(LembreteId id, Long agendamentoId, String paciente, CanalLembrete canal,
                                        StatusLembrete status, LocalDate dataEnvio, String motivoRecusa) {
        Lembrete l = new Lembrete(id, agendamentoId, paciente, canal);
        l.status = status;
        l.dataEnvio = dataEnvio;
        l.motivoRecusa = motivoRecusa;
        return l;
    }

    public void enviar(LocalDate data) {
        if (status != StatusLembrete.PENDENTE) {
            throw new IllegalStateException("Só é possível enviar um lembrete pendente");
        }
        this.status = StatusLembrete.ENVIADO;
        this.dataEnvio = data;
    }

    public void confirmar() {
        if (status != StatusLembrete.ENVIADO) {
            throw new IllegalStateException("Só é possível confirmar um lembrete que já foi enviado");
        }
        this.status = StatusLembrete.CONFIRMADO;
    }

    public void recusar(String motivo) {
        if (status != StatusLembrete.ENVIADO) {
            throw new IllegalStateException("Só é possível recusar um lembrete que já foi enviado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("O motivo da recusa é obrigatório");
        }
        this.status = StatusLembrete.RECUSADO;
        this.motivoRecusa = motivo;
    }

    public void marcarSemResposta() {
        if (status == StatusLembrete.ENVIADO) {
            this.status = StatusLembrete.SEM_RESPOSTA;
        }
    }

    public LembreteId getId() { return id; }
    public Long getAgendamentoId() { return agendamentoId; }
    public String getPaciente() { return paciente; }
    public CanalLembrete getCanal() { return canal; }
    public StatusLembrete getStatus() { return status; }
    public LocalDate getDataEnvio() { return dataEnvio; }
    public String getMotivoRecusa() { return motivoRecusa; }
}
