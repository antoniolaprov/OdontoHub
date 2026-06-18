package com.g4.odontohub.equipe.domain.model;

import java.time.DayOfWeek;

/** Dia da semana de trabalho do colaborador (F12 — controle de disponibilidade). */
public enum DiaSemana {
    SEGUNDA(DayOfWeek.MONDAY),
    TERCA(DayOfWeek.TUESDAY),
    QUARTA(DayOfWeek.WEDNESDAY),
    QUINTA(DayOfWeek.THURSDAY),
    SEXTA(DayOfWeek.FRIDAY),
    SABADO(DayOfWeek.SATURDAY),
    DOMINGO(DayOfWeek.SUNDAY);

    private final DayOfWeek dayOfWeek;

    DiaSemana(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public DayOfWeek toDayOfWeek() {
        return dayOfWeek;
    }

    public static DiaSemana de(DayOfWeek dayOfWeek) {
        for (DiaSemana dia : values()) {
            if (dia.dayOfWeek == dayOfWeek) {
                return dia;
            }
        }
        throw new IllegalArgumentException("Dia da semana inválido: " + dayOfWeek);
    }

    public static DiaSemana fromLabel(String label) {
        return DiaSemana.valueOf(label.trim().toUpperCase());
    }
}
