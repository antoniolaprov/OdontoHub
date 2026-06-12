package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

public enum StatusRecall {
    NA_FILA,
    AGENDADO,
    CONVERTIDO,
    CANCELADO;

    public static StatusRecall fromLabel(String label) {
        return StatusRecall.valueOf(label.trim().toUpperCase().replace(" ", "_"));
    }
}
