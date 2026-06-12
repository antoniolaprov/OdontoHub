package com.g4.odontohub.agendamento.domain.event;

import com.g4.odontohub.agendamento.domain.model.AgendamentoId;

import java.time.LocalDateTime;

public record LembreteConsultaEnviado(
        AgendamentoId agendamentoId,
        Long pacienteId,
        LocalDateTime dataHoraConsulta,
        LocalDateTime dataEnvio,
        String canal) {}
