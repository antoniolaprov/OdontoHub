package com.g4.odontohub.relacionamentopaciente.domain.service;

import com.g4.odontohub.relacionamentopaciente.domain.event.ChecklistRegistrado;

import com.g4.odontohub.relacionamentopaciente.domain.event.EmergenciaIdentificada;
import com.g4.odontohub.relacionamentopaciente.domain.event.FollowupCriado;
import com.g4.odontohub.relacionamentopaciente.domain.model.ChecklistFollowup;
import com.g4.odontohub.relacionamentopaciente.domain.model.FollowupPosOperatorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FollowupService {

    public List<Object> criarFollowup(FollowupPosOperatorio followupPosOperatorio) {
        List<Object> eventos = new ArrayList<>();
        eventos.add(new FollowupCriado(
                followupPosOperatorio.getId(),
                followupPosOperatorio.getPacienteId().pacienteId(),
                followupPosOperatorio.getProcedimentoId().procedimentoId()));
        return eventos;
    }

    public List<Object> registrarChecklist24h(FollowupPosOperatorio followupId,
                                              boolean sangramento,
                                              int nivelDor,
                                              String observacoes,
                                              String responsavel) {
        ChecklistFollowup checklist = new ChecklistFollowup(
                sangramento,
                nivelDor,
                observacoes,
                LocalDateTime.now(),
                responsavel);
        followupId.registrarChecklist24h(checklist);
        return gerarEventosChecklist(followupId, "24h", sangramento, nivelDor);
    }

    public List<Object> registrarChecklist72h(FollowupPosOperatorio followupId,
                                              boolean sangramento,
                                              int nivelDor,
                                              String observacoes,
                                              String responsavel) {
        ChecklistFollowup checklist = new ChecklistFollowup(
                sangramento,
                nivelDor,
                observacoes,
                LocalDateTime.now(),
                responsavel);
        followupId.registrarChecklist72h(checklist);
        return gerarEventosChecklist(followupId, "72h", sangramento, nivelDor);
    }

    private List<Object> gerarEventosChecklist(FollowupPosOperatorio followupPosOperatorio,
                                               String tipoLigacao,
                                               boolean sangramento,
                                               int nivelDor) {
        List<Object> eventos = new ArrayList<>();
        eventos.add(new ChecklistRegistrado(
                followupPosOperatorio.getId(),
                tipoLigacao,
                sangramento,
                nivelDor));

        if (nivelDor > 7 || sangramento) {
            eventos.add(new EmergenciaIdentificada(
                    followupPosOperatorio.getId(),
                    followupPosOperatorio.getPacienteId().pacienteId(),
                    followupPosOperatorio.getDentistaResponsavelId(),
                    sangramento,
                    nivelDor));
        }

        return eventos;
    }
}
