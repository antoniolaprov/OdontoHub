package com.g4.odontohub.relacionamentopaciente.domain.event;

import com.g4.odontohub.relacionamentopaciente.domain.model.FollowupId;

public record EmergenciaIdentificada(
        FollowupId followupId,
        Long pacienteId,
        Long dentistaResponsavelId,
        boolean sangramentoAtivo,
        int nivelDor) {}
