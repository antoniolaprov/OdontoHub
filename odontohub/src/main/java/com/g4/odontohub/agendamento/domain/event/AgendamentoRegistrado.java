package com.g4.odontohub.agendamento.domain.event;

import com.g4.odontohub.agendamento.domain.model.AgendamentoId;
import com.g4.odontohub.agendamento.domain.model.TipoAtendimento;

import java.time.LocalDateTime;

public record AgendamentoRegistrado(
        AgendamentoId agendamentoId,
        Long pacienteId,
        Long dentistaId,
        LocalDateTime dataHora,
        TipoAtendimento tipo) {}