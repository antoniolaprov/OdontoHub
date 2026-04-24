package com.g4.odontohub.infra;

import com.g4.odontohub.tratamento.domain.PlanoTratamento;
import com.g4.odontohub.tratamento.domain.PlanoTratamentoRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryPlanoTratamentoRepository implements PlanoTratamentoRepository {
    private final Map<Long, PlanoTratamento> banco = new HashMap<>();

    @Override
    public void salvar(PlanoTratamento plano) {
        banco.put(plano.getId(), plano);
    }

    @Override
    public PlanoTratamento buscarPorId(Long id) {
        return banco.get(id);
    }

    @Override
    public void deletar(Long id) {
        banco.remove(id);
    }
}