package com.g4.odontohub.relacionamentopaciente.followup.domain.service;

import com.g4.odontohub.relacionamentopaciente.followup.domain.event.ChecklistRegistrado;
import com.g4.odontohub.relacionamentopaciente.followup.domain.event.EmergenciaIdentificada;
import com.g4.odontohub.relacionamentopaciente.followup.domain.event.FollowupCriado;
import com.g4.odontohub.relacionamentopaciente.followup.domain.model.ChecklistFollowup;
import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupId;
import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupPosOperatorio;
import com.g4.odontohub.relacionamentopaciente.followup.domain.repository.FollowupRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FollowupService {

    private final FollowupRepository repositorio;
    private EmergenciaIdentificada ultimaEmergencia;

    public FollowupService(FollowupRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Procedimentos do tipo "Cirurgia" geram tarefas inadiáveis em 24h e 72h. */
    public void processarGatilhosPosOperatorio(String paciente, String tipoProcedimento) {
        if (tipoProcedimento != null && tipoProcedimento.trim().equalsIgnoreCase("Cirurgia")) {
            criarTarefa(paciente, "24h");
            criarTarefa(paciente, "72h");
        }
    }

    public FollowupPosOperatorio criarTarefa(String paciente, String tipoLigacao) {
        FollowupId id = new FollowupId(repositorio.proximoId());
        FollowupPosOperatorio tarefa = new FollowupPosOperatorio(id, paciente, tipoLigacao);
        repositorio.salvar(tarefa);
        DomainEventPublisher.publish(new FollowupCriado(id, paciente, tipoLigacao));
        return tarefa;
    }

    public void registrarChecklist(String paciente, boolean sangramentoAtivo, int nivelDor,
                                   String observacoes, String responsavel, String dentistaResponsavel) {
        FollowupPosOperatorio tarefa = tarefaPendente(paciente)
                .orElseThrow(() -> new IllegalStateException("Sem tarefa de follow-up pendente para: " + paciente));
        ChecklistFollowup checklist = new ChecklistFollowup(
                sangramentoAtivo, nivelDor, observacoes, LocalDate.now(), responsavel);
        tarefa.registrarChecklist(checklist);
        repositorio.salvar(tarefa);
        DomainEventPublisher.publish(new ChecklistRegistrado(
                tarefa.getId(), tarefa.getTipoLigacao(), sangramentoAtivo, nivelDor));

        if (checklist.indicaEmergencia()) {
            ultimaEmergencia = new EmergenciaIdentificada(
                    tarefa.getId(), paciente, dentistaResponsavel, sangramentoAtivo, nivelDor);
            DomainEventPublisher.publish(ultimaEmergencia);
        }
    }

    public boolean existeTarefa(String paciente, String tipoLigacao) {
        return repositorio.todos().stream()
                .anyMatch(t -> t.getPaciente().equals(paciente) && t.getTipoLigacao().equals(tipoLigacao));
    }

    public FollowupPosOperatorio buscarTarefa(String paciente, String tipoLigacao) {
        return repositorio.todos().stream()
                .filter(t -> t.getPaciente().equals(paciente) && t.getTipoLigacao().equals(tipoLigacao))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada para: " + paciente));
    }

    public List<FollowupPosOperatorio> tarefasDe(String paciente) {
        return repositorio.todos().stream()
                .filter(t -> t.getPaciente().equals(paciente))
                .collect(Collectors.toList());
    }

    public EmergenciaIdentificada getUltimaEmergencia() {
        return ultimaEmergencia;
    }

    private Optional<FollowupPosOperatorio> tarefaPendente(String paciente) {
        return repositorio.todos().stream()
                .filter(t -> t.getPaciente().equals(paciente) && !t.isConcluida())
                .findFirst();
    }
}
