package com.g4.odontohub.pagamento.infrastructure.persistence.jpa;

import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;
import com.g4.odontohub.pagamento.domain.repository.PagamentoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter JPA (camada de infraestrutura) que implementa a porta {@link PagamentoRepository}
 * usando Spring Data + H2. É a implementação usada pela aplicação Spring em execução.
 */
@Repository
public class JpaPagamentoRepository implements PagamentoRepository {

    private final SpringDataParcelaPagavelRepository parcelaRepository;
    private final SpringDataPagamentoRepository pagamentoRepository;

    public JpaPagamentoRepository(SpringDataParcelaPagavelRepository parcelaRepository,
                                  SpringDataPagamentoRepository pagamentoRepository) {
        this.parcelaRepository = parcelaRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Override
    public void salvarParcela(ParcelaPagavel parcela) {
        parcelaRepository.save(ParcelaPagavelJpaEntity.fromDomain(parcela));
    }

    @Override
    public ParcelaPagavel buscarParcela(String referencia) {
        return parcelaRepository.findById(referencia)
                .map(ParcelaPagavelJpaEntity::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existeParcela(String referencia) {
        return parcelaRepository.existsById(referencia);
    }

    @Override
    public Collection<ParcelaPagavel> todasParcelas() {
        return parcelaRepository.findAll().stream()
                .map(ParcelaPagavelJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void salvarPagamento(Pagamento pagamento) {
        pagamentoRepository.save(PagamentoJpaEntity.fromDomain(pagamento));
    }

    @Override
    public List<Pagamento> todosPagamentos() {
        return pagamentoRepository.findAll().stream()
                .map(PagamentoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return pagamentoRepository.proximoId();
    }
}
