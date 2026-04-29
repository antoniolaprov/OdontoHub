
package com.g4.odontohub.recall.domain.model;

import java.util.UUID;

public class Agendamento {

    private final String id;
    private final String nomePaciente;
    private final boolean futuroConfirmado;
    private boolean flagConversaoRecall;

    public Agendamento(String nomePaciente, boolean futuroConfirmado) {
        this.id = UUID.randomUUID().toString();
        this.nomePaciente = nomePaciente;
        this.futuroConfirmado = futuroConfirmado;
        this.flagConversaoRecall = false;
    }

    public String getId() {
        return id;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public boolean isFuturoConfirmado() {
        return futuroConfirmado;
    }

    public boolean isFlagConversaoRecall() {
        return flagConversaoRecall;
    }

    public void marcarComoConversaoRecall() {
        this.flagConversaoRecall = true;
    }
}