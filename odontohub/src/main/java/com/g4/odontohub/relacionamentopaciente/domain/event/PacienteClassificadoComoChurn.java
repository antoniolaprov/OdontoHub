package com.g4.odontohub.relacionamentopaciente.domain.event;

import com.g4.odontohub.relacionamentopaciente.domain.model.ChurnId;

public record PacienteClassificadoComoChurn(ChurnId churnId, Long pacienteId) {}
