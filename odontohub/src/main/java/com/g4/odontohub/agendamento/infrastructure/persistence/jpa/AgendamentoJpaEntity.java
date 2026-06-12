package com.g4.odontohub.agendamento.infrastructure.persistence.jpa;

import com.g4.odontohub.agendamento.domain.model.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "agendamento")
public class AgendamentoJpaEntity {

    @Id
    private Long id;
    private Long pacienteId;
    private Long dentistaId;
    private LocalDateTime dataHora;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoAtendimento tipo;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusAgendamento status;
    private String motivoCancelamento;
    private String responsavelAlteracao;
    private LocalDateTime dataUltimaAlteracao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agendamento_historico", joinColumns = @JoinColumn(name = "agendamento_id"))
    private List<EntradaHistoricoEmbeddable> historico = new ArrayList<>();

    protected AgendamentoJpaEntity() {
    }

    public static AgendamentoJpaEntity fromDomain(Agendamento a) {
        AgendamentoJpaEntity e = new AgendamentoJpaEntity();
        e.id = a.getId().id();
        e.pacienteId = a.getPacienteId().id();
        e.dentistaId = a.getDentistaId().id();
        e.dataHora = a.getDataHora();
        e.tipo = a.getTipo();
        e.status = a.getStatus();
        e.motivoCancelamento = a.getMotivoCancelamento();
        e.responsavelAlteracao = a.getResponsavelAlteracao();
        e.dataUltimaAlteracao = a.getDataUltimaAlteracao();
        e.historico = a.getHistorico().stream()
                .map(EntradaHistoricoEmbeddable::fromDomain)
                .collect(Collectors.toList());
        return e;
    }

    public Agendamento toDomain() {
        List<EntradaHistorico> hist = historico.stream()
                .map(EntradaHistoricoEmbeddable::toDomain)
                .collect(Collectors.toList());
        return Agendamento.reconstituir(new AgendamentoId(id), new PacienteId(pacienteId),
                new DentistaId(dentistaId), dataHora, tipo, status, motivoCancelamento,
                responsavelAlteracao, dataUltimaAlteracao, hist);
    }
}
