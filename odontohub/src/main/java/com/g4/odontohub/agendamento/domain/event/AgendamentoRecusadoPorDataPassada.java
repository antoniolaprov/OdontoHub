package com.g4.odontohub.agendamento.domain.event;

import java.time.LocalDateTime;

public record AgendamentoRecusadoPorDataPassada(Long pacienteId, LocalDateTime dataInformada) {}