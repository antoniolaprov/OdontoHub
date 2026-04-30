package com.g4.odontohub.agendamento.domain.event;

import com.g4.odontohub.agendamento.domain.model.AgendamentoId;

import java.time.LocalDateTime;

public record RecusaPacienteRegistrada(
        AgendamentoId agendamentoId,
        Long pacienteId,
        String motivo,
        LocalDateTime dataRecusa) {}
