package com.g4.odontohub.pagamento.domain.repository;

import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;

import java.util.Collection;
import java.util.List;

/**
 * Porta de persistência do contexto de Pagamento (camada de domínio).
 * A implementação concreta vive na camada de infraestrutura.
 */
public interface PagamentoRepository {

    void salvarParcela(ParcelaPagavel parcela);

    ParcelaPagavel buscarParcela(String referencia);

    boolean existeParcela(String referencia);

    Collection<ParcelaPagavel> todasParcelas();

    void salvarPagamento(Pagamento pagamento);

    List<Pagamento> todosPagamentos();
}
