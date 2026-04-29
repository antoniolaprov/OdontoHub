package com.g4.odontohub.agendamento.domain.model;

import java.time.LocalDateTime;

public class Agendamento {

    private final AgendamentoId id;
    private final PacienteId pacienteId;
    private final DentistaId dentistaId;
    private LocalDateTime dataHora;
    private final TipoAtendimento tipo;
    private StatusAgendamento status;
    private String motivoCancelamento;
    private String responsavelAlteracao;
    private LocalDateTime dataUltimaAlteracao;

    public Agendamento(AgendamentoId id, PacienteId pacienteId, DentistaId dentistaId,
                       LocalDateTime dataHora, TipoAtendimento tipo) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.dentistaId = dentistaId;
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.status = StatusAgendamento.AGENDADO;
    }

    public void confirmar(String responsavel) {
        this.status = StatusAgendamento.CONFIRMADO;
        this.responsavelAlteracao = responsavel;
        this.dataUltimaAlteracao = LocalDateTime.now();
    }

    public void cancelar(String motivo, String responsavel) {
        this.status = StatusAgendamento.CANCELADO;
        this.motivoCancelamento = motivo;
        this.responsavelAlteracao = responsavel;
        this.dataUltimaAlteracao = LocalDateTime.now();
    }

    public void remarcar(LocalDateTime novaDataHora, String responsavel) {
        this.dataHora = novaDataHora;
        this.status = StatusAgendamento.REMARCADO;
        this.responsavelAlteracao = responsavel;
        this.dataUltimaAlteracao = LocalDateTime.now();
    }

    public AgendamentoId getId() { return id; }
    public PacienteId getPacienteId() { return pacienteId; }
    public DentistaId getDentistaId() { return dentistaId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public TipoAtendimento getTipo() { return tipo; }
    public StatusAgendamento getStatus() { return status; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public String getResponsavelAlteracao() { return responsavelAlteracao; }
    public LocalDateTime getDataUltimaAlteracao() { return dataUltimaAlteracao; }
}