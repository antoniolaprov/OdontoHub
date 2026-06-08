package com.g4.odontohub.agendamento.domain.model;

import java.time.LocalDateTime;

public class EntradaHistorico {

    private final String acao;
    private final String responsavel;
    private final LocalDateTime dataRegistro;

    public EntradaHistorico(String acao, String responsavel, LocalDateTime dataRegistro) {
        this.acao = acao;
        this.responsavel = responsavel;
        this.dataRegistro = dataRegistro;
    }

    public String getAcao() {
        return acao;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}
