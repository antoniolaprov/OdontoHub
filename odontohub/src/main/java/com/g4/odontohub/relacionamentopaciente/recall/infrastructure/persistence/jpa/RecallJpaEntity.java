package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.CategoriaRecall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.MotivoExclusao;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.RecallId;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.StatusRecall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.TentativaContato;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recall")
public class RecallJpaEntity {

    @Id
    private Long id;
    private String paciente;
    private String procedimentoGatilho;
    private int diasParaRetorno;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusRecall status;
    private boolean flagConversaoRecall;
    private int tentativasContato;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CategoriaRecall categoria;
    private LocalDate dataCriacao;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MotivoExclusao motivoExclusao;
    @Embedded
    private FatoresEmbeddable fatores;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recall_tentativa", joinColumns = @JoinColumn(name = "recall_id"))
    private List<TentativaContatoEmbeddable> historicoTentativas = new ArrayList<>();

    protected RecallJpaEntity() {
    }

    public static RecallJpaEntity fromDomain(Recall r) {
        RecallJpaEntity e = new RecallJpaEntity();
        e.id = r.getId().id();
        e.paciente = r.getPaciente();
        e.procedimentoGatilho = r.getProcedimentoGatilho();
        e.diasParaRetorno = r.getDiasParaRetorno();
        e.status = r.getStatus();
        e.flagConversaoRecall = r.isFlagConversaoRecall();
        e.tentativasContato = r.getTentativasContato();
        e.categoria = r.getCategoria();
        e.dataCriacao = r.getDataCriacao();
        e.motivoExclusao = r.getMotivoExclusao();
        e.fatores = FatoresEmbeddable.fromDomain(r.getFatores());
        e.historicoTentativas = new ArrayList<>();
        for (TentativaContato t : r.getHistoricoTentativas()) {
            e.historicoTentativas.add(TentativaContatoEmbeddable.fromDomain(t));
        }
        return e;
    }

    public Recall toDomain() {
        List<TentativaContato> tentativas = new ArrayList<>();
        if (historicoTentativas != null) {
            for (TentativaContatoEmbeddable t : historicoTentativas) {
                tentativas.add(t.toDomain());
            }
        }
        return Recall.reconstituir(new RecallId(id), paciente, procedimentoGatilho,
                diasParaRetorno, status, flagConversaoRecall, tentativasContato, categoria,
                fatores == null ? null : fatores.toDomain(), dataCriacao, motivoExclusao, tentativas);
    }
}
