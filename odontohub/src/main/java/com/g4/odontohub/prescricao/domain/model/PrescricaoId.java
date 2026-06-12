package com.g4.odontohub.prescricao.domain.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class PrescricaoId {
    private static final AtomicLong counter = new AtomicLong(1);
    private final Long id;

    public PrescricaoId(Long id) {
        this.id = id;
    }

    public static PrescricaoId generate() {
        return new PrescricaoId(counter.getAndIncrement());
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
        PrescricaoId that = (PrescricaoId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PrescricaoId{" + "id=" + id + '}';
    }
}