package com.g4.odontohub.infra;

import com.g4.odontohub.prontuario.domain.Prontuario;
import com.g4.odontohub.prontuario.domain.ProntuarioRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryProntuarioRepository implements ProntuarioRepository {

    private final Map<Long, Prontuario> store = new HashMap<>();

    @Override
    public Prontuario salvar(Prontuario prontuario) {
        store.put(prontuario.getId(), prontuario);
        return prontuario;
    }

    @Override
    public Optional<Prontuario> buscarPorId(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Prontuario> buscarPorPacienteId(Long pacienteId) {
        return store.values().stream()
                .filter(p -> p.getPacienteId().equals(pacienteId))
                .findFirst();
    }
}
