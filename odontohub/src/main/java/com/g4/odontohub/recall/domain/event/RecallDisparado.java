package com.g4.odontohub.recall.domain.event;

public class RecallDisparado {

    private final String nomePaciente;
    private final String procedimento;
    private final int prazoEmDias;

    public RecallDisparado(String nomePaciente, String procedimento, int prazoEmDias) {
        this.nomePaciente = nomePaciente;
        this.procedimento = procedimento;
        this.prazoEmDias = prazoEmDias;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public String getProcedimento() {
        return procedimento;
    }

    public int getPrazoEmDias() {
        return prazoEmDias;
    }
}