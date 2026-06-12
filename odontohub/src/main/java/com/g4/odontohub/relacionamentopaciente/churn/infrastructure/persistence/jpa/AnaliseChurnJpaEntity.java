package com.g4.odontohub.relacionamentopaciente.churn.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.churn.domain.model.AnaliseChurn;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.ChurnId;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.StatusChurn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analise_churn")
public class AnaliseChurnJpaEntity {

    @Id
    private Long id;
    @Column(unique = true)
    private String paciente;
    private boolean possuiAgendamentoFuturo;
    private int mesesSemRetorno;
    private boolean possuiPlanoAtivo;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusChurn statusChurn;

    protected AnaliseChurnJpaEntity() {
    }

    public static AnaliseChurnJpaEntity fromDomain(AnaliseChurn a) {
        AnaliseChurnJpaEntity e = new AnaliseChurnJpaEntity();
        e.id = a.getId().id();
        e.paciente = a.getPaciente();
        e.possuiAgendamentoFuturo = a.isPossuiAgendamentoFuturo();
        e.mesesSemRetorno = a.getMesesSemRetorno();
        e.possuiPlanoAtivo = a.isPossuiPlanoAtivo();
        e.statusChurn = a.getStatusChurn();
        return e;
    }

    public AnaliseChurn toDomain() {
        return AnaliseChurn.reconstituir(new ChurnId(id), paciente, possuiAgendamentoFuturo,
                mesesSemRetorno, possuiPlanoAtivo, statusChurn);
    }
}
