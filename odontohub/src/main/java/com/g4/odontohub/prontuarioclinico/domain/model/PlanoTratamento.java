package com.g4.odontohub.prontuarioclinico.domain.model;

import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlanoTratamento {
    private PlanoId id;
    private Planotratamentopaciente pacienteId;
    private Planotratamentodentista dentistaId;
    private StatusPlano status;
    private int versao;
    private Date dataCriacao;
    private String justificativaEncerramento;
    
    private List<Procedimento> procedimentos = new ArrayList<>();
    private List<LogAuditoria> logsAuditoria = new ArrayList<>();

    public PlanoTratamento(PlanoId id, Planotratamentopaciente pacienteId, Planotratamentodentista dentistaId) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.dentistaId = dentistaId;
        this.status = StatusPlano.EM_ANDAMENTO;
        this.versao = 1;
        this.dataCriacao = new Date();
    }

    public void adicionarProcedimento(Procedimento procedimento) {
        this.procedimentos.add(procedimento);
        this.versao++;
    }

    public Procedimento buscarProcedimento(ProcedimentoId procedimentoId) {
        return procedimentos.stream()
            .filter(p -> p.getId().id().equals(procedimentoId.id()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Procedimento não encontrado"));
    }

    public void excluirProcedimento(ProcedimentoId procedimentoId, String justificativa, String responsavel) {
        Procedimento p = buscarProcedimento(procedimentoId);
        
        if (p.getStatus() == StatusProcedimento.REALIZADO && !p.estaDentroDaJanelaDeCorrecao()) {
            throw new IllegalStateException("Prazo de 24 horas para correção expirado. O registro é imutável");
        }
        
        this.procedimentos.remove(p);
        this.logsAuditoria.add(new LogAuditoria("Exclusão de Procedimento", justificativa, responsavel, new Date()));
    }

    public void encerrarPlano(String justificativa) {
        this.status = StatusPlano.ENCERRADO;
        this.justificativaEncerramento = justificativa;
    }

    public void validarPodeSerExcluido() {
        boolean temProcedimentoImutavel = procedimentos.stream()
            .anyMatch(p -> p.getStatus() == StatusProcedimento.REALIZADO && !p.estaDentroDaJanelaDeCorrecao());
            
        if (temProcedimentoImutavel) {
            throw new IllegalStateException("Plano possui procedimentos no histórico que não podem ser removidos");
        }
    }

    // Getters
    public PlanoId getId() { return id; }
    public Planotratamentopaciente getPacienteId() { return pacienteId; }
    public StatusPlano getStatus() { return status; }
    public int getVersao() { return versao; }
    public String getJustificativaEncerramento() { return justificativaEncerramento; }
    public List<Procedimento> getProcedimentos() { return procedimentos; }
    public List<LogAuditoria> getLogsAuditoria() { return logsAuditoria; }
    public Planotratamentodentista getDentistaId() { return dentistaId; }
    public Date getDataCriacao() { return dataCriacao; }
}   