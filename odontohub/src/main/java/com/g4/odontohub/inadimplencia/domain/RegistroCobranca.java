package com.g4.odontohub.inadimplencia.domain;

import lombok.Getter;

import java.time.LocalDate;

/**
 * Value Object: uma tentativa de cobrança registrada no histórico.
 */
@Getter
public class RegistroCobranca {

    private final LocalDate data;
    private final String responsavel;
    private final String resultado;

    private RegistroCobranca(LocalDate data, String responsavel, String resultado) {
        this.data = data;
        this.responsavel = responsavel;
        this.resultado = resultado;
    }

    public static RegistroCobranca criar(LocalDate data, String responsavel, String resultado) {
        return new RegistroCobranca(data, responsavel, resultado);
    }
}
