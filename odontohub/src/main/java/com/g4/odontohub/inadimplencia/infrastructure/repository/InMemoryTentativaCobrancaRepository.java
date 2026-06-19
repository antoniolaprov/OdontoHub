package com.g4.odontohub.inadimplencia.infrastructure.repository;

import com.g4.odontohub.inadimplencia.domain.model.TentativaCobranca;
import com.g4.odontohub.inadimplencia.domain.repository.TentativaCobrancaRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTentativaCobrancaRepository implements TentativaCobrancaRepository {

    private final Map<String, TentativaCobranca> store = new HashMap<>();

    @Override
    public void salvar(TentativaCobranca tentativa) {
        store.put(tentativa.getId(), tentativa);
    }

    @Override
    public List<TentativaCobranca> listarPorPaciente(String pacienteId) {
        return store.values().stream()
                .filter(t -> t.getPacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }
}
