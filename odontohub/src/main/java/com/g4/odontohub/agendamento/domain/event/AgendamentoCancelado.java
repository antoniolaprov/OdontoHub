package com.g4.odontohub.agendamento.domain.event;

import com.g4.odontohub.agendamento.domain.model.AgendamentoId;

public record AgendamentoCancelado(AgendamentoId agendamentoId, String motivo, String responsavel) {}