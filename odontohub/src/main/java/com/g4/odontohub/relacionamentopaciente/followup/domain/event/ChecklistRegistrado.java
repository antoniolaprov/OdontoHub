package com.g4.odontohub.relacionamentopaciente.followup.domain.event;

import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupId;

public record ChecklistRegistrado(FollowupId followupId, String tipoLigacao, boolean sangramentoAtivo, int nivelDor) {}
