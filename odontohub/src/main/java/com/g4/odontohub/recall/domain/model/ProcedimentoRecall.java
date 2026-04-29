package com.g4.odontohub.recall.domain.model;

public enum ProcedimentoRecall {

    PROFILAXIA(180),
    IMPLANTE(45);

    private final int prazoEmDias;

    ProcedimentoRecall(int prazoEmDias) {
        this.prazoEmDias = prazoEmDias;
    }

    public int getPrazoEmDias() {
        return prazoEmDias;
    }

    public static ProcedimentoRecall fromNome(String nome) {
        for (ProcedimentoRecall p : values()) {
            if (p.name().equalsIgnoreCase(nome.replace(" ", "_"))) {
                return p;
            }
        }
        throw new IllegalArgumentException("Procedimento não mapeado para recall: " + nome);
    }
}