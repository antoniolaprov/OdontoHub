package com.g4.odontohub.agendamento.application;

import com.g4.odontohub.agendamento.domain.event.*;
import com.g4.odontohub.agendamento.domain.model.*;
import com.g4.odontohub.agendamento.domain.service.AgendamentoService;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AgendamentoApplicationService {

    private final AgendamentoService agendamentoService = new AgendamentoService();

    // ACL: simulação de dados cross-context (em memória para testes)
    private final Map<String, Long> pacienteIds = new HashMap<>();
    private final Map<String, Long> dentistaIds = new HashMap<>();
    private final Map<Long, Boolean> pacientesComPlanoAtivo = new HashMap<>();
    private final Map<Long, Boolean> pacientesInadimplentes = new HashMap<>();

    private long nextPacienteId = 1L;
    private long nextDentistaId = 1L;

    public void cadastrarDentista(String nome) {
        dentistaIds.put(nome, nextDentistaId++);
    }

    public void cadastrarPaciente(String nome) {
        pacienteIds.put(nome, nextPacienteId++);
    }

    public void definirPlanoAtivo(String nomePaciente, boolean temPlanoAtivo) {
        pacientesComPlanoAtivo.put(pacienteIds.get(nomePaciente), temPlanoAtivo);
    }

    public void definirInadimplente(String nomePaciente, boolean inadimplente) {
        pacientesInadimplentes.put(pacienteIds.get(nomePaciente), inadimplente);
    }

    public Agendamento registrarAgendamento(String nomePaciente, String nomeDentista, LocalDateTime dataHora) {
        Long pacId = pacienteIds.get(nomePaciente);
        Long denId = dentistaIds.get(nomeDentista);
        boolean planoAtivo = pacientesComPlanoAtivo.getOrDefault(pacId, false);
        boolean inadimplente = pacientesInadimplentes.getOrDefault(pacId, false);

        Agendamento ag = agendamentoService.registrarAgendamento(
                new PacienteId(pacId), new DentistaId(denId), dataHora, planoAtivo, inadimplente);
        DomainEventPublisher.publish(new AgendamentoRegistrado(ag.getId(), pacId, denId, dataHora, ag.getTipo()));
        return ag;
    }

    public void confirmarAgendamento(AgendamentoId id, String responsavel) {
        agendamentoService.confirmarAgendamento(id, responsavel);
        DomainEventPublisher.publish(new AgendamentoConfirmado(id, responsavel));
    }

    public void cancelarAgendamento(AgendamentoId id, String motivo, String responsavel) {
        agendamentoService.cancelarAgendamento(id, motivo, responsavel);
        DomainEventPublisher.publish(new AgendamentoCancelado(id, motivo, responsavel));
    }

    public void remarcarAgendamento(AgendamentoId id, LocalDateTime novaDataHora, String responsavel) {
        agendamentoService.remarcarAgendamento(id, novaDataHora, responsavel);
        DomainEventPublisher.publish(new AgendamentoRemarcado(id, novaDataHora, responsavel));
    }
}