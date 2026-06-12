package com.g4.odontohub.recall.domain.event;

public class RecallConvertido {

    private final String nomePaciente;
    private final String idNovoAgendamento;

    public RecallConvertido(String nomePaciente, String idNovoAgendamento) {
        this.nomePaciente = nomePaciente;
        this.idNovoAgendamento = idNovoAgendamento;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public String getIdNovoAgendamento() {
        return idNovoAgendamento;
    }
}