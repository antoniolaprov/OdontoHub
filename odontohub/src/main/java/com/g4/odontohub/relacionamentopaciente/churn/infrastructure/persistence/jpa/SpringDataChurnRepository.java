package com.g4.odontohub.relacionamentopaciente.churn.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataChurnRepository extends JpaRepository<AnaliseChurnJpaEntity, Long> {

    AnaliseChurnJpaEntity findByPaciente(String paciente);

    @Query("select coalesce(max(a.id), 0) + 1 from AnaliseChurnJpaEntity a")
    long proximoId();
}
