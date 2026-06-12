package com.g4.odontohub.pagamento.infrastructure.persistence;

import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;
import com.g4.odontohub.pagamento.domain.repository.PagamentoRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter de persistência em memória (camada de infraestrutura), usado pelos testes.
 * Implementa a porta {@link PagamentoRepository} definida no domínio.
 */
public class InMemoryPagamentoRepository implements PagamentoRepository {

    private final Map<String, ParcelaPagavel> parcelas = new LinkedHashMap<>();
    private final Map<Long, Pagamento> pagamentos = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvarParcela(ParcelaPagavel parcela) {
        parcelas.put(parcela.getReferencia(), parcela);
    }

    @Override
    public ParcelaPagavel buscarParcela(String referencia) {
        return parcelas.get(referencia);
    }

    @Override
    public boolean existeParcela(String referencia) {
        return parcelas.containsKey(referencia);
    }

    @Override
    public Collection<ParcelaPagavel> todasParcelas() {
        return parcelas.values();
    }

    @Override
    public void salvarPagamento(Pagamento pagamento) {
        pagamentos.put(pagamento.getId().id(), pagamento);
    }

    @Override
    public List<Pagamento> todosPagamentos() {
        return new ArrayList<>(pagamentos.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
