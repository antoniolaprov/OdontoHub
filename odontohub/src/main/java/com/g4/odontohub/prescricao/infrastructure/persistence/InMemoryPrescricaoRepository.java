package com.g4.odontohub.prescricao.infrastructure.persistence;

import com.g4.odontohub.prescricao.domain.model.Prescricao;
import com.g4.odontohub.prescricao.domain.repository.PrescricaoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryPrescricaoRepository implements PrescricaoRepository {

    private final Map<Long, Prescricao> prescricoes = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Prescricao prescricao) {
        prescricoes.put(prescricao.getId().id(), prescricao);
    }

    @Override
    public Prescricao buscar(Long id) {
        return prescricoes.get(id);
    }

    @Override
    public List<Prescricao> todos() {
        return new ArrayList<>(prescricoes.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
