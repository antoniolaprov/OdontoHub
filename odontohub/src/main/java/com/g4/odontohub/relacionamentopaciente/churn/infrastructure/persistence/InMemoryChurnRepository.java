package com.g4.odontohub.relacionamentopaciente.churn.infrastructure.persistence;

import com.g4.odontohub.relacionamentopaciente.churn.domain.model.AnaliseChurn;
import com.g4.odontohub.relacionamentopaciente.churn.domain.repository.ChurnRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryChurnRepository implements ChurnRepository {

    private final Map<String, AnaliseChurn> analises = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(AnaliseChurn analise) {
        analises.put(analise.getPaciente(), analise);
    }

    @Override
    public AnaliseChurn buscarPorPaciente(String paciente) {
        return analises.get(paciente);
    }

    @Override
    public List<AnaliseChurn> todos() {
        return new ArrayList<>(analises.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
