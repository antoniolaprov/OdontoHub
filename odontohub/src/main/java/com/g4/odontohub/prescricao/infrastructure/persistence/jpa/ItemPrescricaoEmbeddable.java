package com.g4.odontohub.prescricao.infrastructure.persistence.jpa;

import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItemPrescricaoEmbeddable {

    @Column(length = 200)
    private String nomeMedicamento;
    private String dosagem;
    private String periodoUso;

    protected ItemPrescricaoEmbeddable() {
    }

    public static ItemPrescricaoEmbeddable fromDomain(ItemPrescricao item) {
        ItemPrescricaoEmbeddable e = new ItemPrescricaoEmbeddable();
        e.nomeMedicamento = item.nomeMedicamento();
        e.dosagem = item.dosagem();
        e.periodoUso = item.periodoUso();
        return e;
    }

    public ItemPrescricao toDomain() {
        return new ItemPrescricao(nomeMedicamento, dosagem, periodoUso);
    }
}
