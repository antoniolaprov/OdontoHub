package com.g4.odontohub.clinica.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataClinicaRepository extends JpaRepository<ClinicaJpaEntity, Long> {

    ClinicaJpaEntity findByEmail(String email);

    ClinicaJpaEntity findByCnpj(String cnpj);

    @Query("select coalesce(max(c.id), 0) + 1 from ClinicaJpaEntity c")
    long proximoId();
}
