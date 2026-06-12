package com.g4.odontohub.relacionamentopaciente.followup.domain.model;

public class FollowupPosOperatorio {

    private final FollowupId id;
    private final String paciente;
    private final String tipoLigacao; // "24h" ou "72h"
    private boolean concluida;
    private ChecklistFollowup checklist;

    public FollowupPosOperatorio(FollowupId id, String paciente, String tipoLigacao) {
        this.id = id;
        this.paciente = paciente;
        this.tipoLigacao = tipoLigacao;
    }

    public void registrarChecklist(ChecklistFollowup checklist) {
        this.checklist = checklist;
        this.concluida = true;
    }

    public FollowupId getId() { return id; }
    public String getPaciente() { return paciente; }
    public String getTipoLigacao() { return tipoLigacao; }
    public boolean isConcluida() { return concluida; }
    public ChecklistFollowup getChecklist() { return checklist; }
}
