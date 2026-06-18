package com.g4.odontohub.agendamento.domain.service;

import com.g4.odontohub.agendamento.domain.model.*;
import com.g4.odontohub.agendamento.domain.repository.AgendamentoRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AgendamentoService {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoRepository repositorio;

    public AgendamentoService(AgendamentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Agendamento registrarAgendamento(PacienteId pacienteId, DentistaId dentistaId,
                                            LocalDateTime dataHora, boolean pacienteTemPlanoAtivo,
                                            boolean pacienteInadimplente, String nomeDentista) {
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível agendar para datas passadas.");
        }
        if (pacienteInadimplente) {
            throw new IllegalArgumentException("Paciente inadimplente");
        }
        if (existeConflitoDeHorario(dentistaId, dataHora)) {
            throw new IllegalArgumentException("Conflito de horário: " + nomeDentista
                    + " já possui agendamento às " + dataHora.format(HORA_FORMATTER) + " neste dia.");
        }

        TipoAtendimento tipo = pacienteTemPlanoAtivo ? TipoAtendimento.RETORNO : TipoAtendimento.CONSULTA;
        AgendamentoId id = new AgendamentoId(repositorio.proximoId());
        Agendamento agendamento = new Agendamento(id, pacienteId, dentistaId, dataHora, tipo);
        repositorio.salvar(agendamento);
        return agendamento;
    }

    public void confirmarAgendamento(AgendamentoId id, String responsavel) {
        Agendamento a = buscar(id);
        a.confirmar(responsavel);
        repositorio.salvar(a);
    }

    public void cancelarAgendamento(AgendamentoId id, String motivo, String responsavel) {
        Agendamento a = buscar(id);
        a.cancelar(motivo, responsavel);
        repositorio.salvar(a);
    }

    public void remarcarAgendamento(AgendamentoId id, LocalDateTime novaDataHora, String responsavel) {
        Agendamento a = buscar(id);
        a.remarcar(novaDataHora, responsavel);
        repositorio.salvar(a);
    }

    public boolean existeConflitoDeHorario(DentistaId dentistaId, LocalDateTime dataHora) {
        return repositorio.todos().stream()
                .filter(a -> a.getDentistaId().equals(dentistaId))
                .filter(a -> a.getStatus() != StatusAgendamento.CANCELADO)
                .anyMatch(a -> a.getDataHora().equals(dataHora));
    }

    /** Leitura de um agendamento pelo identificador; retorna {@code null} se inexistente. */
    public Agendamento buscarPorId(AgendamentoId id) {
        return repositorio.buscarPorId(id);
    }

    private Agendamento buscar(AgendamentoId id) {
        Agendamento a = repositorio.buscarPorId(id);
        if (a == null) {
            throw new IllegalArgumentException("Agendamento não encontrado: " + id.id());
        }
        return a;
    }
}
