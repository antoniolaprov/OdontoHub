package com.g4.odontohub.prescricao.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataPrescricaoRepository extends JpaRepository<PrescricaoJpaEntity, Long> {

    @Query("select coalesce(max(p.id), 0) + 1 from PrescricaoJpaEntity p")
    long proximoId();
}
