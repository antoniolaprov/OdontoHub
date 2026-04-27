package com.g4.odontohub.infra;

import com.g4.odontohub.agendamento.domain.Agendamento;
import com.g4.odontohub.agendamento.domain.AgendamentoRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryAgendamentoRepository implements AgendamentoRepository {

    private final Map<Long, Agendamento> store = new HashMap<>();

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        store.put(agendamento.getId(), agendamento);
        return agendamento;
    }

    @Override
    public Optional<Agendamento> buscarPorId(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existeConflito(LocalDateTime dataHora) {
        return store.values().stream()
                .anyMatch(a -> a.getDataHora().equals(dataHora));
    }
}
