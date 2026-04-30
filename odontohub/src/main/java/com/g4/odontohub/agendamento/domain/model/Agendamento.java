package com.g4.odontohub.agendamento.domain.model;

import java.time.LocalDateTime;

public class Agendamento {

    private final AgendamentoId id;
    private final PacienteId pacienteId;
    private final DentistaId dentistaId;
    private LocalDateTime dataHora;
    private final TipoAtendimento tipo;
    private StatusAgendamento status;
    private RespostaPaciente respostaPaciente;
    private String motivoCancelamento;
    private LocalDateTime dataUltimoLembrete;
    private LocalDateTime dataConfirmacaoPaciente;
    private String canalUltimoLembrete;
    private String responsavelMarcacaoNoShow;
    private LocalDateTime dataMarcacaoNoShow;
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
        this.respostaPaciente = RespostaPaciente.PENDENTE;
    }

    public void confirmar(String responsavel) {
        if (this.status == StatusAgendamento.NAO_COMPARECEU) {
            throw new IllegalArgumentException("Nao e permitido confirmar um agendamento marcado como nao compareceu");
        }
        confirmar(responsavel, LocalDateTime.now());
    }

    public void confirmar(String responsavel, LocalDateTime dataAlteracao) {
        this.status = StatusAgendamento.CONFIRMADO;
        this.responsavelAlteracao = responsavel;
        this.dataUltimaAlteracao = dataAlteracao;
    }

    public void cancelar(String motivo, String responsavel) {
        cancelar(motivo, responsavel, LocalDateTime.now());
    }

    public void cancelar(String motivo, String responsavel, LocalDateTime dataAlteracao) {
        this.status = StatusAgendamento.CANCELADO;
        this.motivoCancelamento = motivo;
        this.responsavelAlteracao = responsavel;
        this.dataUltimaAlteracao = dataAlteracao;
    }

    public void enviarLembrete(String canal) {
        enviarLembrete(canal, LocalDateTime.now());
    }

    public void enviarLembrete(String canal, LocalDateTime dataEnvio) {
        this.canalUltimoLembrete = canal;
        this.dataUltimoLembrete = dataEnvio;
    }

    public void registrarConfirmacaoPaciente() {
        registrarConfirmacaoPaciente(LocalDateTime.now());
    }

    public void registrarConfirmacaoPaciente(LocalDateTime dataConfirmacao) {
        this.respostaPaciente = RespostaPaciente.CONFIRMADO;
        this.dataConfirmacaoPaciente = dataConfirmacao;
    }

    public void registrarRecusaPaciente(String motivo, String responsavel) {
        registrarRecusaPaciente(motivo, responsavel, LocalDateTime.now());
    }

    public void registrarRecusaPaciente(String motivo, String responsavel, LocalDateTime dataRecusa) {
        this.respostaPaciente = RespostaPaciente.RECUSADO;
        cancelar(motivo, responsavel, dataRecusa);
    }

    public void registrarNaoComparecimento(String responsavel) {
        registrarNaoComparecimento(responsavel, LocalDateTime.now());
    }

    public void registrarNaoComparecimento(String responsavel, LocalDateTime dataMarcacao) {
        if (this.status != StatusAgendamento.CONFIRMADO) {
            throw new IllegalArgumentException("Nao e permitido marcar no-show para agendamento nao confirmado");
        }
        if (dataMarcacao.isBefore(this.dataHora)) {
            throw new IllegalArgumentException("Nao e permitido marcar no-show antes do horario da consulta");
        }
        this.status = StatusAgendamento.NAO_COMPARECEU;
        this.responsavelMarcacaoNoShow = responsavel;
        this.dataMarcacaoNoShow = dataMarcacao;
        this.responsavelAlteracao = responsavel;
        this.dataUltimaAlteracao = dataMarcacao;
    }

    public void remarcar(LocalDateTime novaDataHora, String responsavel) {
        remarcar(novaDataHora, responsavel, LocalDateTime.now());
    }

    public void remarcar(LocalDateTime novaDataHora, String responsavel, LocalDateTime dataAlteracao) {
        this.dataHora = novaDataHora;
        this.status = StatusAgendamento.REMARCADO;
        this.responsavelAlteracao = responsavel;
        this.dataUltimaAlteracao = dataAlteracao;
    }

    public AgendamentoId getId() { return id; }
    public PacienteId getPacienteId() { return pacienteId; }
    public DentistaId getDentistaId() { return dentistaId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public TipoAtendimento getTipo() { return tipo; }
    public StatusAgendamento getStatus() { return status; }
    public RespostaPaciente getRespostaPaciente() { return respostaPaciente; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public LocalDateTime getDataUltimoLembrete() { return dataUltimoLembrete; }
    public LocalDateTime getDataConfirmacaoPaciente() { return dataConfirmacaoPaciente; }
    public String getCanalUltimoLembrete() { return canalUltimoLembrete; }
    public String getResponsavelMarcacaoNoShow() { return responsavelMarcacaoNoShow; }
    public LocalDateTime getDataMarcacaoNoShow() { return dataMarcacaoNoShow; }
    public String getResponsavelAlteracao() { return responsavelAlteracao; }
    public LocalDateTime getDataUltimaAlteracao() { return dataUltimaAlteracao; }
}
