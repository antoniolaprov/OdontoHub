package com.g4.odontohub.relacionamentopaciente.followup.application;

import com.g4.odontohub.relacionamentopaciente.followup.domain.event.EmergenciaIdentificada;
import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupPosOperatorio;
import com.g4.odontohub.relacionamentopaciente.followup.domain.repository.FollowupRepository;
import com.g4.odontohub.relacionamentopaciente.followup.domain.service.FollowupService;
import com.g4.odontohub.relacionamentopaciente.followup.infrastructure.persistence.InMemoryFollowupRepository;

import java.util.List;

public class FollowupApplicationService {

    private final FollowupService service;

    public FollowupApplicationService() {
        this(new InMemoryFollowupRepository());
    }

    public FollowupApplicationService(FollowupRepository repositorio) {
        this.service = new FollowupService(repositorio);
    }

    public void processarGatilhos(String paciente, String tipoProcedimento) {
        service.processarGatilhosPosOperatorio(paciente, tipoProcedimento);
    }

    public void criarTarefa(String paciente, String tipoLigacao) {
        service.criarTarefa(paciente, tipoLigacao);
    }

    public void registrarChecklist(String paciente, boolean sangramento, int nivelDor,
                                   String observacoes, String responsavel, String dentistaResponsavel) {
        service.registrarChecklist(paciente, sangramento, nivelDor, observacoes, responsavel, dentistaResponsavel);
    }

    public boolean existeTarefa(String paciente, String tipoLigacao) {
        return service.existeTarefa(paciente, tipoLigacao);
    }

    public FollowupPosOperatorio buscarTarefa(String paciente, String tipoLigacao) {
        return service.buscarTarefa(paciente, tipoLigacao);
    }

    public List<FollowupPosOperatorio> tarefasDe(String paciente) {
        return service.tarefasDe(paciente);
    }

    public EmergenciaIdentificada getUltimaEmergencia() {
        return service.getUltimaEmergencia();
    }
}
