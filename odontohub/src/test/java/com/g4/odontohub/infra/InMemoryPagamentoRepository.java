package com.g4.odontohub.infra;

import com.g4.odontohub.Pagamento.domain.Pagamento;
import com.g4.odontohub.Pagamento.domain.PagamentoRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryPagamentoRepository implements PagamentoRepository {
    private final Map<String, Pagamento> db = new HashMap<>();

    @Override
    public void salvar(Pagamento pagamento) {
        db.put(pagamento.getId(), pagamento);
    }

    @Override
    public boolean existePorPacienteId(String pacienteId) {
        return db.values().stream().anyMatch(p -> p.getPacienteId().equals(pacienteId));
    }
}