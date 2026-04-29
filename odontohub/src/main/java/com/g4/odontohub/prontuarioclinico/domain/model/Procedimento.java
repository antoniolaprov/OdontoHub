package com.g4.odontohub.prontuarioclinico.domain.model;

import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.*;
import java.util.Date;

public class Procedimento {
    private ProcedimentoId id;
    private String nome;
    private String tipoProcedimento;
    private StatusProcedimento status;
    private ProcedimentoAgendamentoVinculado agendamentoVinculado;
    private EvolucaoClinica evolucao;
    private Date dataRealizacao;
    private String executor;
    private String justificativaCancelamento;
    private Date dataConfirmacaoRegistro;

    public Procedimento(ProcedimentoId id, String nome, String tipoProcedimento) {
        this.id = id;
        this.nome = nome;
        this.tipoProcedimento = tipoProcedimento;
        this.status = StatusProcedimento.PENDENTE;
    }

    public void realizar(EvolucaoClinica evolucao, ProcedimentoAgendamentoVinculado agendamento) {
        this.status = StatusProcedimento.REALIZADO;
        this.evolucao = evolucao;
        this.agendamentoVinculado = agendamento;
        this.dataRealizacao = new Date();
        this.dataConfirmacaoRegistro = new Date();
        this.executor = evolucao.executor();
    }

    public void cancelar(String justificativa) {
        this.status = StatusProcedimento.CANCELADO;
        this.justificativaCancelamento = justificativa;
    }

    public void setParaTestesDataConfirmacaoRegistro(Date dataAntiga) {
        this.dataConfirmacaoRegistro = dataAntiga;
    }

    public boolean estaDentroDaJanelaDeCorrecao() {
        if (dataConfirmacaoRegistro == null) return true;
        long horasPassadas = (new Date().getTime() - dataConfirmacaoRegistro.getTime()) / (1000 * 60 * 60);
        return horasPassadas <= 24;
    }

    public ProcedimentoId getId() { return id; }
    public String getNome() { return nome; }
    public StatusProcedimento getStatus() { return status; }
    public ProcedimentoAgendamentoVinculado getAgendamentoVinculado() { return agendamentoVinculado; }
    public EvolucaoClinica getEvolucao() { return evolucao; }
    public String getJustificativaCancelamento() { return justificativaCancelamento; }
    public String getTipoProcedimento() { return tipoProcedimento; }
    public Date getDataRealizacao() { return dataRealizacao; }
    public String getExecutor() { return executor; }
}