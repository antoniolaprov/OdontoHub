package com.g4.odontohub.prescricao.domain.model;

import java.util.Objects;

public class PrescricaoPacienteId {
    private final Long id;

    public PrescricaoPacienteId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PrescricaoPacienteId that = (PrescricaoPacienteId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PrescricaoPacienteId{" + "id=" + id + '}';
    }
}