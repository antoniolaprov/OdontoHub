package com.g4.odontohub.relacionamentopaciente.domain.model;

import java.util.Objects;

public class ChurnId {

    private final Long id;

    public ChurnId(Long id) {
        this.id = Objects.requireNonNull(id, "id é obrigatório");
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChurnId churnId)) return false;
        return id.equals(churnId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
