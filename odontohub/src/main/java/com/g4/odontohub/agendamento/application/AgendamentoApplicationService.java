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
    private final Map<Long, String> pacientesSinalizados = new HashMap<>();

    private long nextPacienteId = 1L;
    private long nextDentistaId = 1L;
    private LocalDateTime dataHoraAtualParaTestes;

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

    public void enviarLembreteConsulta(AgendamentoId id, String canal) {
        agendamentoService.enviarLembreteConsulta(id, canal);
        Agendamento agendamento = agendamentoService.obterAgendamento(id);
        DomainEventPublisher.publish(new LembreteConsultaEnviado(
                id,
                agendamento.getPacienteId().id(),
                agendamento.getDataHora(),
                agendamento.getDataUltimoLembrete(),
                canal));
    }

    public void registrarConfirmacaoPaciente(AgendamentoId id) {
        agendamentoService.registrarConfirmacaoPaciente(id);
        Agendamento agendamento = agendamentoService.obterAgendamento(id);
        DomainEventPublisher.publish(new ConfirmacaoPacienteRegistrada(
                id,
                agendamento.getPacienteId().id(),
                agendamento.getDataConfirmacaoPaciente()));
    }

    public void registrarRecusaPaciente(AgendamentoId id, String motivo, String responsavel) {
        agendamentoService.registrarRecusaPaciente(id, motivo, responsavel);
        Agendamento agendamento = agendamentoService.obterAgendamento(id);
        DomainEventPublisher.publish(new RecusaPacienteRegistrada(
                id,
                agendamento.getPacienteId().id(),
                motivo,
                agendamento.getDataUltimaAlteracao()));
        DomainEventPublisher.publish(new AgendamentoCancelado(id, motivo, responsavel));
    }

    public void marcarNaoComparecimento(AgendamentoId id, String responsavel) {
        agendamentoService.marcarNaoComparecimento(id, responsavel, agora());
        Agendamento agendamento = agendamentoService.obterAgendamento(id);
        DomainEventPublisher.publish(new AgendamentoMarcadoComoNaoCompareceu(
                id,
                agendamento.getPacienteId().id(),
                agendamento.getDataHora(),
                responsavel));

        long quantidadeNoShowsRecentes = agendamentoService.contarNoShowsRecentes(
                agendamento.getPacienteId(),
                agendamento.getDataHora());
        if (quantidadeNoShowsRecentes >= 3) {
            String classificacaoRisco = "Paciente com recorrencia de faltas";
            pacientesSinalizados.put(agendamento.getPacienteId().id(), classificacaoRisco);
            DomainEventPublisher.publish(new PacienteSinalizadoPorNaoComparecimento(
                    agendamento.getPacienteId().id(),
                    Math.toIntExact(quantidadeNoShowsRecentes),
                    classificacaoRisco));
        }
    }

    public Agendamento buscarAgendamentoPorPacienteEData(String nomePaciente, LocalDateTime dataHora) {
        Long pacienteId = pacienteIds.get(nomePaciente);
        if (pacienteId == null) {
            throw new IllegalArgumentException("Paciente nao encontrado");
        }
        return agendamentoService.obterAgendamentoPorPacienteEData(new PacienteId(pacienteId), dataHora);
    }

    public Agendamento obterAgendamento(AgendamentoId id) {
        return agendamentoService.obterAgendamento(id);
    }

    public ResumoOcorrenciasAgenda consultarResumoOcorrencias(String nomePaciente) {
        Long pacienteId = pacienteIds.get(nomePaciente);
        if (pacienteId == null) {
            throw new IllegalArgumentException("Paciente nao encontrado");
        }
        AgendamentoService.ResumoOcorrenciasAgenda resumo =
                agendamentoService.consultarResumoOcorrencias(new PacienteId(pacienteId));
        return new ResumoOcorrenciasAgenda(resumo.quantidadeNoShows(), resumo.quantidadeCancelamentos());
    }

    public String consultarSinalizacaoPaciente(String nomePaciente) {
        Long pacienteId = pacienteIds.get(nomePaciente);
        if (pacienteId == null) {
            throw new IllegalArgumentException("Paciente nao encontrado");
        }
        return pacientesSinalizados.get(pacienteId);
    }

    public void definirDataHoraAtualParaTestes(LocalDateTime dataHoraAtualParaTestes) {
        this.dataHoraAtualParaTestes = dataHoraAtualParaTestes;
    }

    public void limparDataHoraAtualParaTestes() {
        this.dataHoraAtualParaTestes = null;
    }

    private LocalDateTime agora() {
        return dataHoraAtualParaTestes != null ? dataHoraAtualParaTestes : LocalDateTime.now();
    }

    public record ResumoOcorrenciasAgenda(long quantidadeNoShows, long quantidadeCancelamentos) {}
}
