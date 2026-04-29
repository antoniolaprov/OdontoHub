package com.g4.odontohub.relacionamentopaciente.domain.model;

public class FollowupPosOperatorio {

    private final FollowupId id;
    private final FollowupposoperatorioPacienteId pacienteId;
    private final FollowupposoperatorioProcedimentoId procedimentoId;
    private final Long dentistaResponsavelId;
    private boolean ligacao24hConcluida;
    private boolean ligacao72hConcluida;
    private ChecklistFollowup checklist24h;
    private ChecklistFollowup checklist72h;

    public FollowupPosOperatorio(FollowupId id,
                                 FollowupposoperatorioPacienteId pacienteId,
                                 FollowupposoperatorioProcedimentoId procedimentoId,
                                 Long dentistaResponsavelId) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.procedimentoId = procedimentoId;
        this.dentistaResponsavelId = dentistaResponsavelId;
    }

    public void registrarChecklist24h(ChecklistFollowup checklistFollowup) {
        this.checklist24h = checklistFollowup;
        this.ligacao24hConcluida = true;
    }

    public void registrarChecklist72h(ChecklistFollowup checklistFollowup) {
        this.checklist72h = checklistFollowup;
        this.ligacao72hConcluida = true;
    }

    public FollowupId getId() {
        return id;
    }

    public FollowupposoperatorioPacienteId getPacienteId() {
        return pacienteId;
    }

    public FollowupposoperatorioProcedimentoId getProcedimentoId() {
        return procedimentoId;
    }

    public Long getDentistaResponsavelId() {
        return dentistaResponsavelId;
    }

    public boolean isLigacao24hConcluida() {
        return ligacao24hConcluida;
    }

    public boolean isLigacao72hConcluida() {
        return ligacao72hConcluida;
    }

    public ChecklistFollowup getChecklist24h() {
        return checklist24h;
    }

    public ChecklistFollowup getChecklist72h() {
        return checklist72h;
    }
}
