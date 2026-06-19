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
    private String apresentacao;
    private String viaAdministracao;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medicamento_interacao", joinColumns = @JoinColumn(name = "medicamento_id"))
    @Column(name = "interacao", length = 500)
    private List<String> interacoes = new ArrayList<>();

    protected MedicamentoJpaEntity() {
    }

    public static MedicamentoJpaEntity fromDomain(Medicamento m) {
        MedicamentoJpaEntity e = new MedicamentoJpaEntity();
        e.id = m.getId().id();
        e.nomeComercial = m.getNomeComercial();
        e.principioAtivo = m.getPrincipioAtivo();
        e.categoriaTerapeutica = m.getCategoriaTerapeutica();
        e.classeFarmacologica = m.getClasseFarmacologica();
        e.apresentacao = m.getApresentacao();
        e.viaAdministracao = m.getViaAdministracao();
        e.status = m.getStatus();
        e.posologiasPadrao = new ArrayList<>(m.getPosologiasPadrao());
        e.contraindicacoes = new ArrayList<>(m.getContraindicacoes());
        e.interacoes = new ArrayList<>(m.getInteracoes());
        return e;
    }

    public Medicamento toDomain() {
        return Medicamento.reconstituir(new MedicamentoId(id), nomeComercial, principioAtivo,
                categoriaTerapeutica, classeFarmacologica, status, posologiasPadrao, contraindicacoes,
                interacoes, apresentacao, viaAdministracao, null);
    }
}
