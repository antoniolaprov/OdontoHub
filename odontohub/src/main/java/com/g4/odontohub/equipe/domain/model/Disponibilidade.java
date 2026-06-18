package com.g4.odontohub.equipe.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Disponibilidade do colaborador (F12): dias e horário de trabalho e períodos
 * de ausência (férias, afastamento, bloqueios). Agendamentos não podem ser
 * realizados fora desta disponibilidade.
 */
public class Disponibilidade {

    private final Set<DiaSemana> diasDisponiveis;
    private final int horaInicio;
    private final int horaFim;
    private final List<PeriodoAusencia> ausencias;

    public Disponibilidade(Set<DiaSemana> diasDisponiveis, int horaInicio, int horaFim) {
        this(diasDisponiveis, horaInicio, horaFim, new ArrayList<>());
    }

    public Disponibilidade(Set<DiaSemana> diasDisponiveis, int horaInicio, int horaFim,
                           List<PeriodoAusencia> ausencias) {
        if (horaInicio < 0 || horaFim > 24 || horaInicio >= horaFim) {
            throw new IllegalArgumentException("Horário de trabalho inválido");
        }
        this.diasDisponiveis = new LinkedHashSet<>(diasDisponiveis);
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.ausencias = new ArrayList<>(ausencias);
    }

    public void registrarAusencia(PeriodoAusencia ausencia) {
        ausencias.add(ausencia);
    }

    /** Verifica se o colaborador está disponível no momento informado. */
    public boolean estaDisponivelEm(LocalDateTime momento) {
        if (!diasDisponiveis.contains(DiaSemana.de(momento.getDayOfWeek()))) {
            return false;
        }
        int hora = momento.getHour();
        if (hora < horaInicio || hora >= horaFim) {
            return false;
        }
        LocalDate data = momento.toLocalDate();
        return ausencias.stream().noneMatch(a -> a.cobre(data));
    }

    public Set<DiaSemana> getDiasDisponiveis() { return Collections.unmodifiableSet(diasDisponiveis); }
    public int getHoraInicio() { return horaInicio; }
    public int getHoraFim() { return horaFim; }
    public List<PeriodoAusencia> getAusencias() { return Collections.unmodifiableList(ausencias); }
}
