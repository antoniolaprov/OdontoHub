package com.g4.odontohub.relacionamentopaciente.followup.infrastructure.persistence;

import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupPosOperatorio;
import com.g4.odontohub.relacionamentopaciente.followup.domain.repository.FollowupRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryFollowupRepository implements FollowupRepository {

    private final Map<Long, FollowupPosOperatorio> tarefas = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(FollowupPosOperatorio tarefa) {
        tarefas.put(tarefa.getId().id(), tarefa);
    }

    @Override
    public List<FollowupPosOperatorio> todos() {
        return new ArrayList<>(tarefas.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
