package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import com.g4.odontohub.financeiro.domain.repository.LancamentoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link LancamentoRepository}. */
@Repository
public class JpaLancamentoRepository implements LancamentoRepository {

    private final SpringDataLancamentoRepository repository;

    public JpaLancamentoRepository(SpringDataLancamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(LancamentoFinanceiro lancamento) {
        repository.save(LancamentoFinanceiroJpaEntity.fromDomain(lancamento));
    }

    @Override
    public List<LancamentoFinanceiro> todos() {
        return repository.findAll().stream()
                .map(LancamentoFinanceiroJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
