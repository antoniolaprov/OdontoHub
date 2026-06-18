package com.g4.odontohub.equipe.domain.model;

/** Indicadores de desempenho operacional do colaborador (F12). */
public record IndicadoresDesempenho(int atendimentos, int faltas, int conversoes) {

    /** Taxa de conversão = conversões / atendimentos. */
    public double taxaConversao() {
        return atendimentos == 0 ? 0.0 : (double) conversoes / atendimentos;
    }

    /** Produtividade líquida = atendimentos realizados menos faltas. */
    public int produtividade() {
        return atendimentos - faltas;
    }
}
