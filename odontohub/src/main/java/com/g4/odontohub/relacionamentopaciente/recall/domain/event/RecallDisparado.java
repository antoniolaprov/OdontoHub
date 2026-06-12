package com.g4.odontohub.relacionamentopaciente.recall.domain.event;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.RecallId;

public record RecallDisparado(RecallId recallId, String paciente, String procedimentoGatilho, int diasParaRetorno) {}
