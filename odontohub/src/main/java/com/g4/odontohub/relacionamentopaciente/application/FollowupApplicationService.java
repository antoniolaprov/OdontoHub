package com.g4.odontohub.relacionamentopaciente.application;

import com.g4.odontohub.relacionamentopaciente.domain.event.EmergenciaIdentificada;
import com.g4.odontohub.relacionamentopaciente.domain.model.ChecklistFollowup;
import com.g4.odontohub.relacionamentopaciente.domain.model.FollowupId;
import com.g4.odontohub.relacionamentopaciente.domain.model.FollowupPosOperatorio;
import com.g4.odontohub.relacionamentopaciente.domain.model.FollowupposoperatorioPacienteId;
import com.g4.odontohub.relacionamentopaciente.domain.model.FollowupposoperatorioProcedimentoId;
import com.g4.odontohub.relacionamentopaciente.domain.service.FollowupService;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowupApplicationService {

    private final FollowupService followupService = new FollowupService();
    private final Map<String, Long> pacienteIds = new HashMap<>();
    private final Map<Long, String> pacientesPorId = new HashMap<>();
    private final Map<String, Long> dentistaIds = new HashMap<>();
    private final Map<Long, String> dentistasPorId = new HashMap<>();
    private final Map<Long, FollowupPosOperatorio> followupsPorPaciente = new HashMap<>();
    private final List<Object> eventosPublicados = new ArrayList<>();

    private ProcedimentoRealizado ultimoProcedimentoRealizado;
    private EmergenciaIdentificada ultimaEmergenciaIdentificada;
    private long nextPacienteId = 1L;
    private long nextDentistaId = 1L;
    private long nextProcedimentoId = 1L;
    private long nextFollowupId = 1L;

    public FollowupApplicationService() {
        DomainEventPublisher.subscribe(EmergenciaIdentificada.class, evento -> ultimaEmergenciaIdentificada = evento);
    }

    public void cadastrarPaciente(String nomePaciente) {
        garantirPaciente(nomePaciente);
    }

    public void cadastrarDentista(String nomeDentista) {
        garantirDentista(nomeDentista);
    }

    public void registrarProcedimentoRealizado(String nomePaciente, String nomeDentista, String tipoProcedimento) {
        Long pacienteId = garantirPaciente(nomePaciente);
        Long dentistaId = garantirDentista(nomeDentista);
        ultimoProcedimentoRealizado = new ProcedimentoRealizado(nextProcedimentoId++, pacienteId, dentistaId, tipoProcedimento);
    }

    public void processarGatilhosPosOperatorio() {
        if (ultimoProcedimentoRealizado == null) {
            return;
        }

        if (!"cirurgia".equalsIgnoreCase(ultimoProcedimentoRealizado.tipoProcedimento())) {
            return;
        }

        FollowupPosOperatorio followup = new FollowupPosOperatorio(
                new FollowupId(nextFollowupId++),
                new FollowupposoperatorioPacienteId(ultimoProcedimentoRealizado.pacienteId()),
                new FollowupposoperatorioProcedimentoId(ultimoProcedimentoRealizado.procedimentoId()),
                ultimoProcedimentoRealizado.dentistaId());

        followupsPorPaciente.put(ultimoProcedimentoRealizado.pacienteId(), followup);
        publicarEventos(followupService.criarFollowup(followup));
    }

    public void registrarChecklist24h(String nomePaciente,
                                      boolean sangramento,
                                      int nivelDor,
                                      String observacoes,
                                      String responsavel) {
        FollowupPosOperatorio followup = obterFollowupPorPaciente(nomePaciente);
        publicarEventos(followupService.registrarChecklist24h(followup, sangramento, nivelDor, observacoes, responsavel));
    }

    public void registrarChecklist72h(String nomePaciente,
                                      boolean sangramento,
                                      int nivelDor,
                                      String observacoes,
                                      String responsavel) {
        FollowupPosOperatorio followup = obterFollowupPorPaciente(nomePaciente);
        publicarEventos(followupService.registrarChecklist72h(followup, sangramento, nivelDor, observacoes, responsavel));
    }

    public boolean existeTarefa24hPendente(String nomePaciente) {
        FollowupPosOperatorio followup = obterFollowupPorPaciente(nomePaciente);
        return !followup.isLigacao24hConcluida();
    }

    public boolean existeTarefa72hPendente(String nomePaciente) {
        FollowupPosOperatorio followup = obterFollowupPorPaciente(nomePaciente);
        return !followup.isLigacao72hConcluida();
    }

    public boolean existeFollowupParaPaciente(String nomePaciente) {
        Long pacienteId = pacienteIds.get(nomePaciente);
        return pacienteId != null && followupsPorPaciente.containsKey(pacienteId);
    }

    public ChecklistFollowup getChecklist24h(String nomePaciente) {
        return obterFollowupPorPaciente(nomePaciente).getChecklist24h();
    }

    public ChecklistFollowup getChecklist72h(String nomePaciente) {
        return obterFollowupPorPaciente(nomePaciente).getChecklist72h();
    }

    public EmergenciaIdentificada getUltimaEmergenciaIdentificada() {
        return ultimaEmergenciaIdentificada;
    }

    public String getNomeDentistaNotificado() {
        if (ultimaEmergenciaIdentificada == null) {
            return null;
        }
        return dentistasPorId.get(ultimaEmergenciaIdentificada.dentistaResponsavelId());
    }

    public List<Object> getEventosPublicados() {
        return List.copyOf(eventosPublicados);
    }

    private Long garantirPaciente(String nomePaciente) {
        return pacienteIds.computeIfAbsent(nomePaciente, nome -> {
            long pacienteId = nextPacienteId++;
            pacientesPorId.put(pacienteId, nome);
            return pacienteId;
        });
    }

    private Long garantirDentista(String nomeDentista) {
        return dentistaIds.computeIfAbsent(nomeDentista, nome -> {
            long dentistaId = nextDentistaId++;
            dentistasPorId.put(dentistaId, nome);
            return dentistaId;
        });
    }

    private FollowupPosOperatorio obterFollowupPorPaciente(String nomePaciente) {
        Long pacienteId = pacienteIds.get(nomePaciente);
        if (pacienteId == null) {
            throw new IllegalStateException("Paciente não cadastrado: " + nomePaciente);
        }

        FollowupPosOperatorio followup = followupsPorPaciente.get(pacienteId);
        if (followup == null) {
            throw new IllegalStateException("Follow-up não encontrado para o paciente");
        }
        return followup;
    }

    private void publicarEventos(List<Object> eventos) {
        eventos.forEach(this::publicarEvento);
    }

    private void publicarEvento(Object evento) {
        eventosPublicados.add(evento);
        DomainEventPublisher.publish(evento);
    }

    private record ProcedimentoRealizado(Long procedimentoId, Long pacienteId, Long dentistaId, String tipoProcedimento) {}
}
