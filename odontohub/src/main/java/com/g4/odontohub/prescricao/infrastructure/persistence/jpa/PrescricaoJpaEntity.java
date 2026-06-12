package com.g4.odontohub.prescricao.infrastructure.persistence.jpa;

import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;
import com.g4.odontohub.prescricao.domain.model.PrescricaoId;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "prescricao")
public class PrescricaoJpaEntity {

    @Id
    private Long id;
    private String paciente;
    private String dentista;
    private LocalDate dataPrescricao;
    @Column(length = 1000)
    private String observacoesTerapeuticas;
    private Long prescricaoOrigemId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "prescricao_item", joinColumns = @JoinColumn(name = "prescricao_id"))
    private List<ItemPrescricaoEmbeddable> itens = new ArrayList<>();

    protected PrescricaoJpaEntity() {
    }

    public static PrescricaoJpaEntity fromDomain(Prescricao p) {
        PrescricaoJpaEntity e = new PrescricaoJpaEntity();
        e.id = p.getId().id();
        e.paciente = p.getPaciente();
        e.dentista = p.getDentista();
        e.dataPrescricao = p.getDataPrescricao();
        e.observacoesTerapeuticas = p.getObservacoesTerapeuticas();
        e.prescricaoOrigemId = p.getPrescricaoOrigemId();
        e.itens = p.getItens().stream()
                .map(ItemPrescricaoEmbeddable::fromDomain)
                .collect(Collectors.toList());
        return e;
    }

    public Prescricao toDomain() {
        List<ItemPrescricao> itensDominio = itens.stream()
                .map(ItemPrescricaoEmbeddable::toDomain)
                .collect(Collectors.toList());
        Prescricao p = new Prescricao(new PrescricaoId(id), paciente, dentista, dataPrescricao,
                itensDominio, prescricaoOrigemId);
        p.definirObservacoes(observacoesTerapeuticas);
        return p;
    }
}
