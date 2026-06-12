package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataLancamentoRepository extends JpaRepository<LancamentoFinanceiroJpaEntity, Long> {

    @Query("select coalesce(max(l.id), 0) + 1 from LancamentoFinanceiroJpaEntity l")
    long proximoId();
}
