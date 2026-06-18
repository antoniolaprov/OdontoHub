package com.g4.odontohub.equipe.domain.model;

import java.time.LocalDate;

/** Período de ausência do colaborador (férias, afastamento, bloqueio temporário) — F12. */
public record PeriodoAusencia(LocalDate inicio, LocalDate fim, TipoAusencia tipo, String motivo) {

    public boolean cobre(LocalDate data) {
        return data != null && !data.isBefore(inicio) && !data.isAfter(fim);
    }
}
