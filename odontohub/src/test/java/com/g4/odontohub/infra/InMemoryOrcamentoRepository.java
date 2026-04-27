package com.g4.odontohub.infra;


import com.g4.odontohub.orcamento.domain.Orcamento;
import com.g4.odontohub.orcamento.domain.OrcamentoRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryOrcamentoRepository implements OrcamentoRepository {
    private final Map<Long, Orcamento> banco = new HashMap<>();

    @Override
    public void salvar(Orcamento orcamento) {
        banco.put(orcamento.getId(), orcamento);
    }

    @Override
    public Orcamento buscarPorId(Long id) {
        return banco.get(id);
    }

    @Override
    public Optional<Orcamento> buscarPorPlanoTratamentoId(Long planoTratamentoId) {
        return banco.values().stream()
                .filter(o -> o.getPlanoTratamentoId().equals(planoTratamentoId))
                .findFirst();
    }
}