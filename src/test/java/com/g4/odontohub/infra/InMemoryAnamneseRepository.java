package com.g4.odontohub.infra;

import com.g4.odontohub.prontuario.domain.Anamnese;
import com.g4.odontohub.prontuario.domain.AnamneseRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryAnamneseRepository implements AnamneseRepository {

    private final Map<Long, Anamnese> store = new HashMap<>();

    @Override
    public Anamnese salvar(Anamnese anamnese) {
        store.put(anamnese.getPacienteId(), anamnese);
        return anamnese;
    }

    @Override
    public Optional<Anamnese> buscarPorPacienteId(Long pacienteId) {
        return Optional.ofNullable(store.get(pacienteId));
    }
}
