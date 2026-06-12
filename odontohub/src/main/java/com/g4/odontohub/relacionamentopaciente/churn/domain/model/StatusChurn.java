package com.g4.odontohub.relacionamentopaciente.churn.domain.model;

public enum StatusChurn {
    ATIVO,
    ZONA_DE_RISCO,
    EVADIDO,
    REATIVADO;

    public static StatusChurn fromLabel(String label) {
        return StatusChurn.valueOf(label.trim().toUpperCase().replace(" ", "_"));
    }
}
