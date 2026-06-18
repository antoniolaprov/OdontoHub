package com.g4.odontohub.equipe.domain.model;

/** Tipo de período de ausência do colaborador (F12). */
public enum TipoAusencia {
    FERIAS,
    AFASTAMENTO,
    BLOQUEIO_TEMPORARIO;

    public static TipoAusencia fromLabel(String label) {
        return TipoAusencia.valueOf(label.trim().toUpperCase().replace(" ", "_").replace("-", "_"));
    }
}
