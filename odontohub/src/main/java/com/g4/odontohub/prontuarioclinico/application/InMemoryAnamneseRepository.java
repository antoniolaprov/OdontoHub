package com.g4.odontohub.prontuarioclinico.application;

import com.g4.odontohub.prontuarioclinico.domain.model.Anamnese;
import java.util.HashMap;
import java.util.Map;

public class InMemoryAnamneseRepository implements AnamneseRepository {
    private final Map<Long, Anamnese> db = new HashMap<>();

    @Override
    public void salvar(Anamnese anamnese) {
        db.put(anamnese.getPacienteId().pacienteId(), anamnese);
    }

    @Override
    public Anamnese buscarPorPacienteId(Long pacienteId) {
        return db.get(pacienteId);
    }
}