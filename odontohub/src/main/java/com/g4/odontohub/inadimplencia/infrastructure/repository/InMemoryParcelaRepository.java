package com.g4.odontohub.inadimplencia.infrastructure.repository;

import com.g4.odontohub.inadimplencia.domain.model.Parcela;
import com.g4.odontohub.inadimplencia.domain.model.StatusParcela;
import com.g4.odontohub.inadimplencia.domain.repository.ParcelaRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryParcelaRepository implements ParcelaRepository {

    private final Map<String, Parcela> store = new HashMap<>();

    @Override
    public void salvar(Parcela parcela) {
        store.put(parcela.getId(), parcela);
    }

    @Override
    public Optional<Parcela> buscarPorId(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Parcela> listarPorPaciente(String pacienteId) {
        return store.values().stream()
                .filter(p -> p.getPacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Parcela> listarPorPacienteEStatus(String pacienteId, StatusParcela status) {
        return store.values().stream()
                .filter(p -> p.getPacienteId().equals(pacienteId))
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Parcela> listarVencidas() {
        return store.values().stream()
                .filter(p -> p.getStatus() == StatusParcela.VENCIDA
                        || (p.getStatus() == StatusParcela.EM_ABERTO
                            && LocalDate.now().isAfter(p.getVencimento())))
                .collect(Collectors.toList());
    }
}
