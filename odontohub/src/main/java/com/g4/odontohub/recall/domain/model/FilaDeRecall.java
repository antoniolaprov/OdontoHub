package com.g4.odontohub.recall.domain.model;

public class FilaDeRecall {

    private final String nomePaciente;
    private final int prazoEmDias;
    private StatusRecall status;
    private String idAgendamentoCancelador;

    public FilaDeRecall(String nomePaciente, int prazoEmDias) {
        this.nomePaciente = nomePaciente;
        this.prazoEmDias = prazoEmDias;
        this.status = StatusRecall.NA_FILA;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public int getPrazoEmDias() {
        return prazoEmDias;
    }

    public StatusRecall getStatus() {
        return status;
    }

    public String getIdAgendamentoCancelador() {
        return idAgendamentoCancelador;
    }

    public void cancelar(String idAgendamento) {
        this.status = StatusRecall.CANCELADO;
        this.idAgendamentoCancelador = idAgendamento;
    }

    public void converter() {
        this.status = StatusRecall.CONVERTIDO;
    }
}