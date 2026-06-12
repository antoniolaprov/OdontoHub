package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryRecallRepository implements RecallRepository {

    private final Map<Long, Recall> recalls = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Recall recall) {
        recalls.put(recall.getId().id(), recall);
    }

    @Override
    public List<Recall> todos() {
        return new ArrayList<>(recalls.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
