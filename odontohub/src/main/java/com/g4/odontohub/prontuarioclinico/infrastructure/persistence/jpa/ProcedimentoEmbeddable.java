package com.g4.odontohub.prontuarioclinico.infrastructure.persistence.jpa;

import com.g4.odontohub.prontuarioclinico.domain.model.Procedimento;
import com.g4.odontohub.prontuarioclinico.domain.model.StatusProcedimento;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.EvolucaoClinica;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.ProcedimentoAgendamentoVinculado;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.ProcedimentoId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Embeddable
public class ProcedimentoEmbeddable {

    private Long procedimentoId;
    private String nome;
    private String tipoProcedimento;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusProcedimento status;
    private Long agendamentoVinculadoId;
    @Column(length = 2000)
    private String evolucaoDescricao;
    private String evolucaoExecutor;
    @Temporal(TemporalType.TIMESTAMP)
    private Date evolucaoData;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRealizacao;
    private String executor;
    private String justificativaCancelamento;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataConfirmacaoRegistro;

    protected ProcedimentoEmbeddable() {
    }

    public static ProcedimentoEmbeddable fromDomain(Procedimento p) {
        ProcedimentoEmbeddable e = new ProcedimentoEmbeddable();
        e.procedimentoId = p.getId().id();
        e.nome = p.getNome();
        e.tipoProcedimento = p.getTipoProcedimento();
        e.status = p.getStatus();
        if (p.getAgendamentoVinculado() != null) {
            e.agendamentoVinculadoId = p.getAgendamentoVinculado().agendamentoVinculadoId();
        }
        if (p.getEvolucao() != null) {
            e.evolucaoDescricao = p.getEvolucao().descricaoTecnica();
            e.evolucaoExecutor = p.getEvolucao().executor();
            e.evolucaoData = p.getEvolucao().dataRegistro();
        }
        e.dataRealizacao = p.getDataRealizacao();
        e.executor = p.getExecutor();
        e.justificativaCancelamento = p.getJustificativaCancelamento();
        // dataConfirmacaoRegistro não é exposto por getter no domínio; recomposto a partir de dataRealizacao.
        e.dataConfirmacaoRegistro = p.getDataRealizacao();
        return e;
    }

    public Procedimento toDomain() {
        EvolucaoClinica evolucao = (evolucaoExecutor != null || evolucaoDescricao != null || evolucaoData != null)
                ? new EvolucaoClinica(evolucaoDescricao, evolucaoExecutor, evolucaoData)
                : null;
        ProcedimentoAgendamentoVinculado agendamento = agendamentoVinculadoId != null
                ? new ProcedimentoAgendamentoVinculado(agendamentoVinculadoId)
                : null;
        return Procedimento.reconstituir(new ProcedimentoId(procedimentoId), nome, tipoProcedimento, status,
                agendamento, evolucao, dataRealizacao, executor, justificativaCancelamento, dataConfirmacaoRegistro);
    }
}
