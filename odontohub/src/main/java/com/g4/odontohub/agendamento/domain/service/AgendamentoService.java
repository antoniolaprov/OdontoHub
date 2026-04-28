package com.g4.odontohub.agendamento.domain.service;

import com.g4.odontohub.agendamento.domain.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AgendamentoService {

    private final Map<AgendamentoId, Agendamento> agendamentos = new HashMap<>();
    private long nextId = 1L;

    public Agendamento registrarAgendamento(PacienteId pacienteId, DentistaId dentistaId,
                                            LocalDateTime dataHora, boolean pacienteTemPlanoAtivo,
                                            boolean pacienteInadimplente) {
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é permitido registrar agendamentos em datas passadas");
        }
        if (pacienteInadimplente) {
            throw new IllegalArgumentException("Paciente restrito: possui parcelas em atraso há mais de 30 dias");
        }
        if (existeConflitoDeHorario(dentistaId, dataHora)) {
            throw new IllegalArgumentException("Conflito de horário: o dentista já possui um agendamento neste horário");
        }

        TipoAtendimento tipo = pacienteTemPlanoAtivo ? TipoAtendimento.RETORNO : TipoAtendimento.CONSULTA;
        AgendamentoId id = new AgendamentoId(nextId++);
        Agendamento agendamento = new Agendamento(id, pacienteId, dentistaId, dataHora, tipo);
        agendamentos.put(id, agendamento);
        return agendamento;
    }

    public void confirmarAgendamento(AgendamentoId id, String responsavel) {
        agendamentos.get(id).confirmar(responsavel);
    }

    public void cancelarAgendamento(AgendamentoId id, String motivo, String responsavel) {
        agendamentos.get(id).cancelar(motivo, responsavel);
    }

    public void remarcarAgendamento(AgendamentoId id, LocalDateTime novaDataHora, String responsavel) {
        agendamentos.get(id).remarcar(novaDataHora, responsavel);
    }

    public boolean existeConflitoDeHorario(DentistaId dentistaId, LocalDateTime dataHora) {
        return agendamentos.values().stream()
                .filter(a -> a.getDentistaId().equals(dentistaId))
                .filter(a -> a.getStatus() != StatusAgendamento.CANCELADO)
                .anyMatch(a -> a.getDataHora().equals(dataHora));
    }
}