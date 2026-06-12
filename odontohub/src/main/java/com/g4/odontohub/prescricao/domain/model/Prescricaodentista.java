package com.g4.odontohub.prescricao.domain.model;

import java.util.Objects;

public class Prescricaodentista {
    private final Long dentistaId;

    public Prescricaodentista(Long dentistaId) {
        this.dentistaId = dentistaId;
    }

    public Long getDentistaId() {
        return dentistaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Prescricaodentista that = (Prescricaodentista) o;
        return Objects.equals(dentistaId, that.dentistaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dentistaId);
    }

    @Override
    public String toString() {
        return "Prescricaodentista{" + "dentistaId=" + dentistaId + '}';
    }
}