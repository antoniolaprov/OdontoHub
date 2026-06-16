package com.g4.odontohub.confirmacao.lembrete.infrastructure.persistence.jpa;

import com.g4.odontohub.confirmacao.lembrete.domain.model.CanalLembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.model.Lembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.model.LembreteId;
import com.g4.odontohub.confirmacao.lembrete.domain.model.StatusLembrete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "lembrete")
public class LembreteJpaEntity {

    @Id
    private Long id;
    private Long agendamentoId;
    private String paciente;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CanalLembrete canal;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusLembrete status;
    private LocalDate dataEnvio;
    @Column(length = 1000)
    private String motivoRecusa;

    protected LembreteJpaEntity() {
    }

    public static LembreteJpaEntity fromDomain(Lembrete l) {
        LembreteJpaEntity e = new LembreteJpaEntity();
        e.id = l.getId().id();
        e.agendamentoId = l.getAgendamentoId();
        e.paciente = l.getPaciente();
        e.canal = l.getCanal();
        e.status = l.getStatus();
        e.dataEnvio = l.getDataEnvio();
        e.motivoRecusa = l.getMotivoRecusa();
        return e;
    }

    public Lembrete toDomain() {
        return Lembrete.reconstituir(new LembreteId(id), agendamentoId, paciente, canal,
                status, dataEnvio, motivoRecusa);
    }
}
