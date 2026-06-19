package com.g4.odontohub.inadimplencia.infrastructure.repository;

import com.g4.odontohub.inadimplencia.domain.model.Paciente;
import com.g4.odontohub.inadimplencia.domain.repository.PacienteRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryPacienteRepository implements PacienteRepository {

    private final Map<String, Paciente> store = new HashMap<>();

    @Override
    public void salvar(Paciente paciente) {
        store.put(paciente.getId(), paciente);
    }

    @Override
    public Optional<Paciente> buscarPorId(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Paciente> buscarPorNome(String nome) {
        return store.values().stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst();
    }

    @Override
    public List<Paciente> listarTodos() {
        return new ArrayList<>(store.values());
    }
}
