package com.g4.odontohub.confirmacao.naocomparecimento.infrastructure.persistence.jpa;

import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimento;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimentoId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "nao_comparecimento")
public class NaoComparecimentoJpaEntity {

    @Id
    private Long id;
    private Long agendamentoId;
    private String paciente;
    private LocalDate dataConsulta;
    private boolean justificada;
    @Column(length = 1000)
    private String justificativa;
    private LocalDate dataRegistro;

    protected NaoComparecimentoJpaEntity() {
    }

    public static NaoComparecimentoJpaEntity fromDomain(NaoComparecimento r) {
        NaoComparecimentoJpaEntity e = new NaoComparecimentoJpaEntity();
        e.id = r.getId().id();
        e.agendamentoId = r.getAgendamentoId();
        e.paciente = r.getPaciente();
        e.dataConsulta = r.getDataConsulta();
        e.justificada = r.isJustificada();
        e.justificativa = r.getJustificativa();
        e.dataRegistro = r.getDataRegistro();
        return e;
    }

    public NaoComparecimento toDomain() {
        return NaoComparecimento.reconstituir(new NaoComparecimentoId(id), agendamentoId, paciente,
                dataConsulta, justificada, justificativa, dataRegistro);
    }
}
