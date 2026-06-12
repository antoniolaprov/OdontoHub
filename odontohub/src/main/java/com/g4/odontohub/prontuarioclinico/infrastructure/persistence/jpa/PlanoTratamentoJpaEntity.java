package com.g4.odontohub.prontuarioclinico.infrastructure.persistence.jpa;

import com.g4.odontohub.prontuarioclinico.domain.model.PlanoTratamento;
import com.g4.odontohub.prontuarioclinico.domain.model.Procedimento;
import com.g4.odontohub.prontuarioclinico.domain.model.StatusPlano;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.LogAuditoria;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.PlanoId;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.Planotratamentodentista;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.Planotratamentopaciente;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "plano_tratamento")
public class PlanoTratamentoJpaEntity {

    @Id
    private Long id;
    private Long pacienteId;
    private Long dentistaId;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusPlano status;
    private int versao;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCriacao;
    @Column(length = 1000)
    private String justificativaEncerramento;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plano_procedimento", joinColumns = @JoinColumn(name = "plano_id"))
    private List<ProcedimentoEmbeddable> procedimentos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plano_log_auditoria", joinColumns = @JoinColumn(name = "plano_id"))
    private List<LogAuditoriaEmbeddable> logs = new ArrayList<>();

    protected PlanoTratamentoJpaEntity() {
    }

    public static PlanoTratamentoJpaEntity fromDomain(PlanoTratamento p) {
        PlanoTratamentoJpaEntity e = new PlanoTratamentoJpaEntity();
        e.id = p.getId().id();
        e.pacienteId = p.getPacienteId().pacienteId();
        e.dentistaId = p.getDentistaId().dentistaId();
        e.status = p.getStatus();
        e.versao = p.getVersao();
        e.dataCriacao = p.getDataCriacao();
        e.justificativaEncerramento = p.getJustificativaEncerramento();
        e.procedimentos = p.getProcedimentos().stream()
                .map(ProcedimentoEmbeddable::fromDomain).collect(Collectors.toList());
        e.logs = p.getLogsAuditoria().stream()
                .map(LogAuditoriaEmbeddable::fromDomain).collect(Collectors.toList());
        return e;
    }

    public PlanoTratamento toDomain() {
        List<Procedimento> procs = procedimentos.stream()
                .map(ProcedimentoEmbeddable::toDomain).collect(Collectors.toList());
        List<LogAuditoria> logsDom = logs.stream()
                .map(LogAuditoriaEmbeddable::toDomain).collect(Collectors.toList());
        return PlanoTratamento.reconstituir(new PlanoId(id), new Planotratamentopaciente(pacienteId),
                new Planotratamentodentista(dentistaId), status, versao, dataCriacao,
                justificativaEncerramento, procs, logsDom);
    }
}
