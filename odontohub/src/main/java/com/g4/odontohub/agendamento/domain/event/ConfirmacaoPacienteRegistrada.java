package com.g4.odontohub.agendamento.domain.event;

import com.g4.odontohub.agendamento.domain.model.AgendamentoId;

import java.time.LocalDateTime;

public record ConfirmacaoPacienteRegistrada(
        AgendamentoId agendamentoId,
        Long pacienteId,
        LocalDateTime dataConfirmacao) {}
