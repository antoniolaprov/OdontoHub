package com.g4.odontohub.prescricao.domain.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class ItemPrescricaoId {
    private static final AtomicLong counter = new AtomicLong(1);
    private final Long id;

    public ItemPrescricaoId(Long id) {
        this.id = id;
    }

    public static ItemPrescricaoId generate() {
        return new ItemPrescricaoId(counter.getAndIncrement());
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
        ItemPrescricaoId that = (ItemPrescricaoId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemPrescricaoId{" + "id=" + id + '}';
    }
}