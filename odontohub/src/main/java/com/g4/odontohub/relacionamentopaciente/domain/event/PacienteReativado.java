package com.g4.odontohub.relacionamentopaciente.domain.event;

import com.g4.odontohub.relacionamentopaciente.domain.model.ChurnId;

public record PacienteReativado(ChurnId churnId, Long pacienteId) {}
