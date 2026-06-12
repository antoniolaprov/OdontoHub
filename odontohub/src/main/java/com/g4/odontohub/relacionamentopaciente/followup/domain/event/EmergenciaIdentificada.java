package com.g4.odontohub.relacionamentopaciente.followup.domain.event;

import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupId;

public record EmergenciaIdentificada(FollowupId followupId, String paciente, String dentistaResponsavel,
                                     boolean sangramentoAtivo, int nivelDor) {}
