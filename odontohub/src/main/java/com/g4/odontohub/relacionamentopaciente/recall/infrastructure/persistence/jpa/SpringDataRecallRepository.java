package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataRecallRepository extends JpaRepository<RecallJpaEntity, Long> {

    @Query("select coalesce(max(r.id), 0) + 1 from RecallJpaEntity r")
    long proximoId();
}
