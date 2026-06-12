package com.g4.odontohub.relacionamentopaciente.domain.event;

import com.g4.odontohub.relacionamentopaciente.domain.model.FollowupId;

public record ChecklistRegistrado(
        FollowupId followupId,
        String tipoLigacao,
        boolean sangramentoAtivo,
        int nivelDor) {}
