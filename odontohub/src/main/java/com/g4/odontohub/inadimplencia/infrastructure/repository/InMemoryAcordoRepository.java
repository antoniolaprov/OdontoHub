package com.g4.odontohub.inadimplencia.infrastructure.repository;

import com.g4.odontohub.inadimplencia.domain.model.Acordo;
import com.g4.odontohub.inadimplencia.domain.model.StatusAcordo;
import com.g4.odontohub.inadimplencia.domain.repository.AcordoRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryAcordoRepository implements AcordoRepository {

    private final Map<String, Acordo> store = new HashMap<>();

    @Override
    public void salvar(Acordo acordo) {
        store.put(acordo.getId(), acordo);
    }

    @Override
    public Optional<Acordo> buscarPorId(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Acordo> listarPorPaciente(String pacienteId) {
        return store.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Acordo> buscarAtivoPorPaciente(String pacienteId) {
        return store.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .filter(a -> a.getStatus() == StatusAcordo.ATIVO)
                .findFirst();
    }

    @Override
    public List<Acordo> listarPorStatus(StatusAcordo status) {
        return store.values().stream()
                .filter(a -> a.getStatus() == status)
                .collect(Collectors.toList());
    }
}
