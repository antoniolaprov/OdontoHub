package com.g4.odontohub.confirmacao.lembrete.infrastructure.persistence;

import com.g4.odontohub.confirmacao.lembrete.domain.model.Lembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.model.LembreteId;
import com.g4.odontohub.confirmacao.lembrete.domain.repository.LembreteRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryLembreteRepository implements LembreteRepository {

    private final Map<Long, Lembrete> lembretes = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Lembrete lembrete) {
        lembretes.put(lembrete.getId().id(), lembrete);
    }

    @Override
    public Lembrete buscarPorId(LembreteId id) {
        return lembretes.get(id.id());
    }

    @Override
    public Lembrete buscarPorAgendamento(Long agendamentoId) {
        return lembretes.values().stream()
                .filter(l -> l.getAgendamentoId().equals(agendamentoId))
                .reduce((primeiro, segundo) -> segundo)
                .orElse(null);
    }

    @Override
    public List<Lembrete> todos() {
        return new ArrayList<>(lembretes.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
