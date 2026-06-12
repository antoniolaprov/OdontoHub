package com.g4.odontohub.financeiro.infrastructure.persistence;

import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import com.g4.odontohub.financeiro.domain.repository.LancamentoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryLancamentoRepository implements LancamentoRepository {

    private final Map<Long, LancamentoFinanceiro> lancamentos = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(LancamentoFinanceiro lancamento) {
        lancamentos.put(lancamento.getId().id(), lancamento);
    }

    @Override
    public List<LancamentoFinanceiro> todos() {
        return new ArrayList<>(lancamentos.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
