package com.g4.odontohub.relacionamentopaciente.domain.model;

import java.util.Objects;

public class AnaliseChurnPacienteId {

    private final Long pacienteId;

    public AnaliseChurnPacienteId(Long pacienteId) {
        this.pacienteId = Objects.requireNonNull(pacienteId, "pacienteId é obrigatório");
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnaliseChurnPacienteId that)) return false;
        return pacienteId.equals(that.pacienteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pacienteId);
    }
}
