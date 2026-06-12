package com.g4.odontohub.prescricao.domain.model;

import java.util.Objects;

public class ItemPrescricao {
    private final ItemPrescricaoId id;
    private final String nomeMedicamento;
    private final String dosagem;
    private final String periodoUso;

    public ItemPrescricao(ItemPrescricaoId id, String nomeMedicamento, String dosagem, String periodoUso) {
        this.id = Objects.requireNonNull(id, "ID do item de prescrição não pode ser nulo");
        this.nomeMedicamento = Objects.requireNonNull(nomeMedicamento, "Nome do medicamento não pode ser nulo");
        this.dosagem = Objects.requireNonNull(dosagem, "Dosagem não pode ser nula");
        this.periodoUso = Objects.requireNonNull(periodoUso, "Período de uso não pode ser nulo");
    }

    public static ItemPrescricao criar(String nomeMedicamento, String dosagem, String periodoUso) {
        return new ItemPrescricao(
                ItemPrescricaoId.generate(),
                nomeMedicamento,
                dosagem,
                periodoUso);
    }

    public ItemPrescricaoId getId() {
        return id;
    }

    public String getNomeMedicamento() {
        return nomeMedicamento;
    }

    public String getDosagem() {
        return dosagem;
    }

    public String getPeriodoUso() {
        return periodoUso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemPrescricao that = (ItemPrescricao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemPrescricao{" +
                "id=" + id +
                ", nomeMedicamento='" + nomeMedicamento + '\'' +
                ", dosagem='" + dosagem + '\'' +
                ", periodoUso='" + periodoUso + '\'' +
                '}';
    }
}