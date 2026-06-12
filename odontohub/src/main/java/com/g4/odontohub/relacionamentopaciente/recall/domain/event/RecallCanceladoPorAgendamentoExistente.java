package com.g4.odontohub.relacionamentopaciente.recall.domain.event;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.RecallId;

public record RecallCanceladoPorAgendamentoExistente(RecallId recallId, Long agendamentoExistenteId) {}
