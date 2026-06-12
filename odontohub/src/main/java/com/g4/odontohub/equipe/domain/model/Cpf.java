package com.g4.odontohub.equipe.domain.model;

public class Cpf {

    private final String valor;

    public Cpf(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório para o cadastro de colaboradores");
        }
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}