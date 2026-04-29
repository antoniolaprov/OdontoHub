package com.g4.odontohub.relacionamentopaciente.domain.event;

import com.g4.odontohub.relacionamentopaciente.domain.model.FollowupId;

public record FollowupCriado(FollowupId followupId, Long pacienteId, Long procedimentoId) {}
