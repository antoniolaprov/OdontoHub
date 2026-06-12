package com.g4.odontohub.equipe.domain.event;

public class ColaboradorDesativado {

    private final String nomeColaborador;

    public ColaboradorDesativado(String nomeColaborador) {
        this.nomeColaborador = nomeColaborador;
    }

    public String getNomeColaborador() {
        return nomeColaborador;
    }
}