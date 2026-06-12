package com.g4.odontohub.agendamento.infrastructure.persistence;

import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.AgendamentoId;
import com.g4.odontohub.agendamento.domain.repository.AgendamentoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryAgendamentoRepository implements AgendamentoRepository {

    private final Map<Long, Agendamento> agendamentos = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Agendamento agendamento) {
        agendamentos.put(agendamento.getId().id(), agendamento);
    }

    @Override
    public Agendamento buscarPorId(AgendamentoId id) {
        return agendamentos.get(id.id());
    }

    @Override
    public List<Agendamento> todos() {
        return new ArrayList<>(agendamentos.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
