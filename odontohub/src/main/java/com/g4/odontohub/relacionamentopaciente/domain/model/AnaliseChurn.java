package com.g4.odontohub.relacionamentopaciente.domain.model;

import java.time.LocalDate;

public class AnaliseChurn {

    private final ChurnId id;
    private final AnaliseChurnPacienteId pacienteId;
    private LocalDate ultimoAgendamento;
    private StatusChurn statusChurn;
    private String motivoCancelamento;
    private boolean reativado;
    private LocalDate dataReativacao;

    public AnaliseChurn(ChurnId id, AnaliseChurnPacienteId pacienteId) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.statusChurn = StatusChurn.ATIVO;
        this.reativado = false;
    }

    public void atualizarStatus(StatusChurn novoStatus) {
        this.statusChurn = novoStatus;
        if (novoStatus != StatusChurn.REATIVADO) {
            this.reativado = false;
            this.dataReativacao = null;
        }
    }

    public void registrarReativacao(LocalDate dataReativacao) {
        this.statusChurn = StatusChurn.REATIVADO;
        this.reativado = true;
        this.dataReativacao = dataReativacao;
        this.ultimoAgendamento = dataReativacao;
    }

    public void registrarUltimoAgendamento(LocalDate ultimoAgendamento) {
        this.ultimoAgendamento = ultimoAgendamento;
    }

    public void registrarMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public ChurnId getId() {
        return id;
    }

    public AnaliseChurnPacienteId getPacienteId() {
        return pacienteId;
    }

    public LocalDate getUltimoAgendamento() {
        return ultimoAgendamento;
    }

    public StatusChurn getStatusChurn() {
        return statusChurn;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public boolean isReativado() {
        return reativado;
    }

    public LocalDate getDataReativacao() {
        return dataReativacao;
    }
}
