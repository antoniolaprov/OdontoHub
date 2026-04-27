package com.g4.odontohub.infra;

import com.g4.odontohub.inadimplencia.domain.PagamentoParcelado;
import com.g4.odontohub.inadimplencia.domain.PagamentoParceladoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação in-memory de PagamentoParceladoRepository para uso nos testes BDD.
 */
public class InMemoryPagamentoParceladoRepository implements PagamentoParceladoRepository {

    private final Map<String, PagamentoParcelado> banco = new HashMap<>();

    @Override
    public void salvar(PagamentoParcelado pagamento) {
        banco.put(pagamento.getNomePaciente(), pagamento);
    }

    @Override
    public Optional<PagamentoParcelado> buscarPorPaciente(String nomePaciente) {
        return Optional.ofNullable(banco.get(nomePaciente));
    }

    @Override
    public List<PagamentoParcelado> listarTodos() {
        return new ArrayList<>(banco.values());
    }
}
