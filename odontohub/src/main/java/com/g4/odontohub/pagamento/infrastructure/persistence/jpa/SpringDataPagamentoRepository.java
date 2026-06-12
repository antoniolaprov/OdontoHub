package com.g4.odontohub.pagamento.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataPagamentoRepository extends JpaRepository<PagamentoJpaEntity, Long> {

    @Query("select coalesce(max(p.id), 0) + 1 from PagamentoJpaEntity p")
    long proximoId();
}
