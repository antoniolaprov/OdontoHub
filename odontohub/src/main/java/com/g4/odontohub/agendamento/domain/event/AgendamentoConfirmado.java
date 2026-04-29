package com.g4.odontohub.agendamento.domain.event;

import com.g4.odontohub.agendamento.domain.model.AgendamentoId;

public record AgendamentoConfirmado(AgendamentoId agendamentoId, String responsavel) {}