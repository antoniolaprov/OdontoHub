package com.g4.odontohub.medicamento.infrastructure.persistence.jpa;

import com.g4.odontohub.medicamento.domain.model.Medicamento;
import com.g4.odontohub.medicamento.domain.model.MedicamentoId;
import com.g4.odontohub.medicamento.domain.model.StatusMedicamento;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicamento")
public class MedicamentoJpaEntity {

    @Id
    private Long id;
    private String nomeComercial;
    private String principioAtivo;
    private String categoriaTerapeutica;
    private String classeFarmacologica;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusMedicamento status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medicamento_posologia", joinColumns = @JoinColumn(name = "medicamento_id"))
    @Column(name = "posologia", length = 500)
    private List<String> posologiasPadrao = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medicamento_contraindicacao", joinColumns = @JoinColumn(name = "medicamento_id"))
    @Column(name = "contraindicacao", length = 500)
    private List<String> contraindicacoes = new ArrayList<>();

    protected MedicamentoJpaEntity() {
    }

    public static MedicamentoJpaEntity fromDomain(Medicamento m) {
        MedicamentoJpaEntity e = new MedicamentoJpaEntity();
        e.id = m.getId().id();
        e.nomeComercial = m.getNomeComercial();
        e.principioAtivo = m.getPrincipioAtivo();
        e.categoriaTerapeutica = m.getCategoriaTerapeutica();
        e.classeFarmacologica = m.getClasseFarmacologica();
        e.status = m.getStatus();
        e.posologiasPadrao = new ArrayList<>(m.getPosologiasPadrao());
        e.contraindicacoes = new ArrayList<>(m.getContraindicacoes());
        return e;
    }

    public Medicamento toDomain() {
        return Medicamento.reconstituir(new MedicamentoId(id), nomeComercial, principioAtivo,
                categoriaTerapeutica, classeFarmacologica, status, posologiasPadrao, contraindicacoes);
    }
}
