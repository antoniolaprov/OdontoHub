package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

/**
 * Nível de priorização da fila de recall (F07).
 * Determina a ordem de contato: procedimentos de maior risco clínico são
 * contatados primeiro.
 */
public enum NivelPrioridade {
    ALTA(3),
    MEDIA(2),
    BAIXA(1);

    private final int peso;

    NivelPrioridade(int peso) {
        this.peso = peso;
    }

    public int peso() {
        return peso;
    }

    /** Classifica a prioridade a partir do procedimento que disparou o recall. */
    public static NivelPrioridade doProcedimento(String procedimento) {
        return switch (procedimento == null ? "" : procedimento.trim().toUpperCase()) {
            case "CIRURGIA", "IMPLANTE" -> ALTA;
            case "ORTODONTIA" -> MEDIA;
            default -> BAIXA;
        };
    }
}
