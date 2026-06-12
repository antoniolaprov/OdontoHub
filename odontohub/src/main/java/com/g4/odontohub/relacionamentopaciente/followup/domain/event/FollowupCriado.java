package com.g4.odontohub.relacionamentopaciente.followup.domain.event;

import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupId;

public record FollowupCriado(FollowupId followupId, String paciente, String tipoLigacao) {}
