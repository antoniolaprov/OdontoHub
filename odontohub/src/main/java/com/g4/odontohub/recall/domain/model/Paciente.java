package com.g4.odontohub.recall.domain.model;

public class Paciente {

    private final String nome;

    public Paciente(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}