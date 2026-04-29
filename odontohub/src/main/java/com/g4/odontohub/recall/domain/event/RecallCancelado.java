package com.g4.odontohub.recall.domain.event;

public class RecallCancelado {

    private final String nomePaciente;
    private final String idAgendamentoExistente;

    public RecallCancelado(String nomePaciente, String idAgendamentoExistente) {
        this.nomePaciente = nomePaciente;
        this.idAgendamentoExistente = idAgendamentoExistente;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public String getIdAgendamentoExistente() {
        return idAgendamentoExistente;
    }
}