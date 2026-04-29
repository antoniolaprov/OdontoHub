package com.g4.odontohub.prontuarioclinico.application;

import com.g4.odontohub.prontuarioclinico.domain.model.PlanoTratamento;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.PlanoId;
import java.util.HashMap;
import java.util.Map;

public class InMemoryPlanoTratamentoRepository implements PlanoTratamentoRepository {
    private final Map<Long, PlanoTratamento> db = new HashMap<>();

    @Override
    public void salvar(PlanoTratamento plano) { db.put(plano.getId().id(), plano); }

    @Override
    public PlanoTratamento buscarPorId(PlanoId id) { return db.get(id.id()); }

    @Override
    public PlanoTratamento buscarPorPacienteId(Long pacienteId) {
        return db.values().stream()
            .filter(p -> p.getPacienteId().pacienteId().equals(pacienteId))
            .findFirst().orElse(null);
    }

    @Override
    public void remover(PlanoId id) { db.remove(id.id()); }
}