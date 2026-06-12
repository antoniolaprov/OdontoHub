package com.g4.odontohub.pagamento.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataParcelaPagavelRepository extends JpaRepository<ParcelaPagavelJpaEntity, String> {
}
