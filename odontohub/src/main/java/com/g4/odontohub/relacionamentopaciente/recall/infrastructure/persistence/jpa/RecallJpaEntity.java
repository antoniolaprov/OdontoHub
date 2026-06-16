package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.RecallId;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.StatusRecall;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
        return e;
    }

    public Recall toDomain() {
        return Recall.reconstituir(new RecallId(id), paciente, procedimentoGatilho,
                diasParaRetorno, status, flagConversaoRecall, tentativasContato);
    }
}
