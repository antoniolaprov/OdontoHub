package com.g4.odontohub.relacionamentopaciente.application;

import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.AgendamentoId;
import com.g4.odontohub.agendamento.domain.model.DentistaId;
import com.g4.odontohub.agendamento.domain.model.PacienteId;
import com.g4.odontohub.agendamento.domain.model.StatusAgendamento;
import com.g4.odontohub.agendamento.domain.model.TipoAtendimento;
import com.g4.odontohub.relacionamentopaciente.domain.event.PacienteEntrandoZonaDeRisco;
import com.g4.odontohub.relacionamentopaciente.domain.model.AnaliseChurn;
import com.g4.odontohub.relacionamentopaciente.domain.model.AnaliseChurnPacienteId;
import com.g4.odontohub.relacionamentopaciente.domain.model.ChurnId;
import com.g4.odontohub.relacionamentopaciente.domain.model.StatusChurn;
import com.g4.odontohub.relacionamentopaciente.domain.service.ChurnService;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChurnApplicationService {

    private final ChurnService churnService = new ChurnService();

    private final Map<String, Long> pacienteIds = new HashMap<>();
    private final Map<Long, String> nomesPacientes = new HashMap<>();
    private final Map<Long, AnaliseChurn> analisesPorPaciente = new HashMap<>();
    private final Map<Long, Integer> mesesSemRetorno = new HashMap<>();
    private final Map<Long, Boolean> pacientesComPlanoAtivo = new HashMap<>();
    private final Map<Long, Boolean> pacientesComAgendamentosFuturos = new HashMap<>();
    private final Map<Long, Agendamento> agendamentosPorId = new HashMap<>();
    private final List<Object> eventosPublicados = new ArrayList<>();

    private long nextPacienteId = 1L;
    private long nextChurnId = 1L;
    private long nextAgendamentoId = 1L;
    private double valorMedioHora;
    private double ultimaReceitaPerdida;
    private String ultimaMensagemErro;
    private PacienteEntrandoZonaDeRisco ultimoAlertaInatividade;
    private Agendamento ultimoAgendamento;

    public ChurnApplicationService() {
        DomainEventPublisher.subscribe(PacienteEntrandoZonaDeRisco.class, evento -> ultimoAlertaInatividade = evento);
    }

    public void definirValorMedioHora(double valorMedioHora) {
        this.valorMedioHora = valorMedioHora;
    }

    public void cadastrarPaciente(String nomePaciente) {
        garantirPaciente(nomePaciente);
    }

    public void definirSemAgendamentosFuturos(String nomePaciente) {
        Long pacienteId = garantirPaciente(nomePaciente);
        pacientesComAgendamentosFuturos.put(pacienteId, false);
    }

    public void definirMesesSemRetorno(String nomePaciente, int meses) {
        Long pacienteId = garantirPaciente(nomePaciente);
        mesesSemRetorno.put(pacienteId, meses);
    }

    public void definirPlanoAtivo(String nomePaciente, boolean planoAtivo) {
        Long pacienteId = garantirPaciente(nomePaciente);
        pacientesComPlanoAtivo.put(pacienteId, planoAtivo);
    }

    public void definirStatusChurn(String nomePaciente, StatusChurn statusChurn) {
        Long pacienteId = garantirPaciente(nomePaciente);
        AnaliseChurn analiseChurn = garantirAnalise(pacienteId);
        analiseChurn.atualizarStatus(statusChurn);
    }

    public void recalcularStatusChurn() {
        for (Long pacienteId : nomesPacientes.keySet()) {
            AnaliseChurn analiseChurn = garantirAnalise(pacienteId);
            List<Object> eventos = churnService.recalcularStatusChurn(
                    analiseChurn,
                    pacientesComAgendamentosFuturos.getOrDefault(pacienteId, false),
                    mesesSemRetorno.getOrDefault(pacienteId, 0),
                    pacientesComPlanoAtivo.getOrDefault(pacienteId, false));
            publicarEventos(eventos);
        }
    }

    public void registrarNovoAgendamento(String nomePaciente) {
        Long pacienteId = garantirPaciente(nomePaciente);
        pacientesComAgendamentosFuturos.put(pacienteId, true);

        AnaliseChurn analiseChurn = garantirAnalise(pacienteId);
        Agendamento agendamento = criarAgendamentoConfirmado(pacienteId);
        analiseChurn.registrarUltimoAgendamento(agendamento.getDataHora().toLocalDate());
        ultimoAgendamento = agendamento;

        if (analiseChurn.getStatusChurn() == StatusChurn.EVADIDO) {
            publicarEvento(churnService.registrarNovoAgendamento(analiseChurn, LocalDate.now()));
        }
    }

    public void criarAgendamentoConfirmadoPara(String nomePaciente) {
        Long pacienteId = garantirPaciente(nomePaciente);
        ultimoAgendamento = criarAgendamentoConfirmado(pacienteId);
    }

    public void registrarNaoComparecimento(String nomePaciente) {
        Long pacienteId = garantirPaciente(nomePaciente);
        Agendamento agendamento = obterOuCriarAgendamentoConfirmadoDoPaciente(pacienteId);
        agendamento.registrarNaoComparecimento("Sistema");
        ultimoAgendamento = agendamento;
    }

    public void cancelarAgendamentoSemCategoria(String nomePaciente) {
        cancelarAgendamentoComCategoria(nomePaciente, "");
    }

    public void cancelarAgendamentoComCategoria(String nomePaciente, String motivoCancelamento) {
        Long pacienteId = garantirPaciente(nomePaciente);
        AnaliseChurn analiseChurn = garantirAnalise(pacienteId);
        Agendamento agendamento = obterOuCriarAgendamentoConfirmadoDoPaciente(pacienteId);
        try {
            churnService.registrarCancelamentoComMotivo(analiseChurn, motivoCancelamento);
            agendamento.cancelar(motivoCancelamento, "Recepcionista");
            ultimoAgendamento = agendamento;
            ultimaMensagemErro = null;
        } catch (IllegalArgumentException e) {
            ultimaMensagemErro = e.getMessage();
            throw e;
        }
    }

    public double calcularReceitaPerdida(Long dentistaId, int horasDeAgendaOciosa) {
        ultimaReceitaPerdida = churnService.calcularReceitaPerdida(dentistaId, horasDeAgendaOciosa, valorMedioHora);
        return ultimaReceitaPerdida;
    }

    public AnaliseChurn buscarAnalisePorPaciente(String nomePaciente) {
        Long pacienteId = garantirPaciente(nomePaciente);
        return garantirAnalise(pacienteId);
    }

    public PacienteEntrandoZonaDeRisco getUltimoAlertaInatividade() {
        return ultimoAlertaInatividade;
    }

    public double getUltimaReceitaPerdida() {
        return ultimaReceitaPerdida;
    }

    public String getUltimaMensagemErro() {
        return ultimaMensagemErro;
    }

    public Agendamento getUltimoAgendamento() {
        return ultimoAgendamento;
    }

    public long contarNaoComparecimentos() {
        return agendamentosPorId.values().stream()
                .filter(a -> a.getStatus() == StatusAgendamento.NAO_COMPARECEU)
                .count();
    }

    public long contarCancelamentosAntecipados() {
        return agendamentosPorId.values().stream()
                .filter(a -> a.getStatus() == StatusAgendamento.CANCELADO)
                .count();
    }

    public List<Object> getEventosPublicados() {
        return List.copyOf(eventosPublicados);
    }

    private Long garantirPaciente(String nomePaciente) {
        return pacienteIds.computeIfAbsent(nomePaciente, nome -> {
            long pacienteId = nextPacienteId++;
            nomesPacientes.put(pacienteId, nome);
            analisesPorPaciente.put(pacienteId, new AnaliseChurn(
                    new ChurnId(nextChurnId++),
                    new AnaliseChurnPacienteId(pacienteId)));
            return pacienteId;
        });
    }

    private AnaliseChurn garantirAnalise(Long pacienteId) {
        return analisesPorPaciente.computeIfAbsent(
                pacienteId,
                id -> new AnaliseChurn(new ChurnId(nextChurnId++), new AnaliseChurnPacienteId(id)));
    }

    private Agendamento criarAgendamentoConfirmado(Long pacienteId) {
        Agendamento agendamento = new Agendamento(
                new AgendamentoId(nextAgendamentoId++),
                new PacienteId(pacienteId),
                new DentistaId(1L),
                LocalDateTime.now().plusDays(7),
                TipoAtendimento.RETORNO);
        agendamento.confirmar("Sistema");
        agendamentosPorId.put(agendamento.getId().id(), agendamento);
        return agendamento;
    }

    private Agendamento encontrarAgendamentoConfirmadoDoPaciente(Long pacienteId) {
        return agendamentosPorId.values().stream()
                .filter(a -> a.getPacienteId().id().equals(pacienteId))
                .filter(a -> a.getStatus() == StatusAgendamento.CONFIRMADO)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Agendamento confirmado não encontrado para o paciente"));
    }

    private Agendamento obterOuCriarAgendamentoConfirmadoDoPaciente(Long pacienteId) {
        return agendamentosPorId.values().stream()
                .filter(a -> a.getPacienteId().id().equals(pacienteId))
                .filter(a -> a.getStatus() == StatusAgendamento.CONFIRMADO)
                .findFirst()
                .orElseGet(() -> criarAgendamentoConfirmado(pacienteId));
    }

    private void publicarEventos(List<Object> eventos) {
        eventos.forEach(this::publicarEvento);
    }

    private void publicarEvento(Object evento) {
        eventosPublicados.add(evento);
        DomainEventPublisher.publish(evento);
    }
}
