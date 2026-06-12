package com.g4.odontohub.relacionamentopaciente.churn.domain.event;

import com.g4.odontohub.relacionamentopaciente.churn.domain.model.ChurnId;

public record PacienteClassificadoComoChurn(ChurnId churnId, String paciente) {}
