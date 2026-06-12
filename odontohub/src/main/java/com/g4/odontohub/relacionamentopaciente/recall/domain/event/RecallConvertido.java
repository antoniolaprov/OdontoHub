package com.g4.odontohub.relacionamentopaciente.recall.domain.event;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.RecallId;

public record RecallConvertido(RecallId recallId, Long agendamentoGeradoId) {}
