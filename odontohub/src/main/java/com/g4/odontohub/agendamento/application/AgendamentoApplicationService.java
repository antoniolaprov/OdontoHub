package com.g4.odontohub.agendamento.application;

import com.g4.odontohub.agendamento.domain.event.*;
import com.g4.odontohub.agendamento.domain.model.*;
import com.g4.odontohub.agendamento.domain.repository.AgendamentoRepository;
import com.g4.odontohub.agendamento.domain.service.AgendamentoService;
import com.g4.odontohub.agendamento.infrastructure.persistence.InMemoryAgendamentoRepository;
import com.g4.odontohub.cadastropaciente.application.PacienteApplicationService;
import com.g4.odontohub.cadastropaciente.domain.model.StatusPaciente;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgendamentoApplicationService {

    private final AgendamentoService agendamentoService;
    /** Leitura cross-context (ACL): null nos testes BDD, que não verificam a regra de Restrito. */
    private final PacienteApplicationService pacienteApplicationService;

    public AgendamentoApplicationService() {
        this(new InMemoryAgendamentoRepository(), null);
    }

    public AgendamentoApplicationService(AgendamentoRepository repositorio) {
        this(repositorio, null);
    }

    public AgendamentoApplicationService(AgendamentoRepository repositorio,
                                         PacienteApplicationService pacienteApplicationService) {
        this.agendamentoService = new AgendamentoService(repositorio);
        this.pacienteApplicationService = pacienteApplicationService;
    }

    // ACL: simulação de dados cross-context (em memória para testes)
    private final Map<String, Long> pacienteIds = new HashMap<>();
    private final Map<String, Long> dentistaIds = new HashMap<>();
    private final Map<Long, String> nomesDentistas = new HashMap<>();
    private final Map<Long, String> nomesPacientes = new HashMap<>();
    private final Map<Long, Boolean> pacientesComPlanoAtivo = new HashMap<>();
    private final Map<Long, Boolean> pacientesInadimplentes = new HashMap<>();

    private long nextPacienteId = 1L;
    private long nextDentistaId = 1L;

    public void cadastrarDentista(String nome) {
        Long id = nextDentistaId++;
        dentistaIds.put(nome, id);
        nomesDentistas.put(id, nome);
    }

    public void cadastrarPaciente(String nome) {
        Long id = nextPacienteId++;
        pacienteIds.put(nome, id);
        nomesPacientes.put(id, nome);
    }

    /** Resolve o nome do paciente a partir do id (ACL local); usado para leitura/listagem. */
    public String nomeDoPaciente(Long id) {
        return nomesPacientes.getOrDefault(id, "Paciente #" + id);
    }

    /** Resolve o nome do dentista a partir do id (ACL local); usado para leitura/listagem. */
    public String nomeDoDentista(Long id) {
        return nomesDentistas.getOrDefault(id, "Dentista #" + id);
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
        boolean restrito = pacienteEstaRestrito(nomePaciente);

        Agendamento ag = agendamentoService.registrarAgendamento(
                new PacienteId(pacId), new DentistaId(denId), dataHora, planoAtivo, inadimplente, restrito,
                nomesDentistas.getOrDefault(denId, nomeDentista));
        DomainEventPublisher.publish(new AgendamentoRegistrado(ag.getId(), pacId, denId, dataHora, ag.getTipo()));
        return ag;
    }

    /** Consulta o status real do paciente no contexto de Cadastro de Pacientes (F13). */
    private boolean pacienteEstaRestrito(String nomePaciente) {
        if (pacienteApplicationService == null) {
            return false;
        }
        try {
            return pacienteApplicationService.statusDe(nomePaciente) == StatusPaciente.RESTRITO;
        } catch (IllegalArgumentException pacienteNaoEncontrado) {
            return false;
        }
    }

    /** Leitura de um agendamento pelo identificador; retorna {@code null} se inexistente. */
    public Agendamento buscarPorId(AgendamentoId id) {
        return agendamentoService.buscarPorId(id);
    }

    /** Lista todos os agendamentos (leitura para o frontend). */
    public List<Agendamento> listarTodos() {
        return agendamentoService.listarTodos();
    }

    public void confirmarAgendamento(AgendamentoId id, String responsavel) {
        agendamentoService.confirmarAgendamento(id, responsavel);
        DomainEventPublisher.publish(new AgendamentoConfirmado(id, responsavel));
    }

    /**
     * Integração F16 → F1: confirma o agendamento a partir da confirmação do paciente
     * (evento {@code ConsultaConfirmadaPeloPaciente}). É best-effort: se o agendamento
     * não existir neste contexto ou já estiver em estado terminal, a integração é ignorada.
     *
     * @return {@code true} se o agendamento foi efetivamente confirmado
     */
    public boolean confirmarPorConfirmacaoDoPaciente(Long agendamentoId, String responsavel) {
        if (agendamentoId == null) {
            return false;
        }
        try {
            confirmarAgendamento(new AgendamentoId(agendamentoId), responsavel);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
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
