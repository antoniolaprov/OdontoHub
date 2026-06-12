package com.g4.odontohub.equipe.domain.event;

public class ColaboradorReativado {

    private final String nomeColaborador;

    public ColaboradorReativado(String nomeColaborador) {
        this.nomeColaborador = nomeColaborador;
    }

    public String getNomeColaborador() {
        return nomeColaborador;
    }
}