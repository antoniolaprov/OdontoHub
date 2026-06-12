package com.g4.odontohub.medicamento.infrastructure.persistence;

import com.g4.odontohub.medicamento.domain.model.Medicamento;
import com.g4.odontohub.medicamento.domain.repository.MedicamentoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryMedicamentoRepository implements MedicamentoRepository {

    private final Map<Long, Medicamento> medicamentos = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Medicamento medicamento) {
        medicamentos.put(medicamento.getId().id(), medicamento);
    }

    @Override
    public Medicamento buscarPorNome(String nomeComercial) {
        return medicamentos.values().stream()
                .filter(m -> m.getNomeComercial().equals(nomeComercial))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Medicamento> todos() {
        return new ArrayList<>(medicamentos.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
