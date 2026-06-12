package com.g4.odontohub.cadastropaciente.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataPacienteRepository extends JpaRepository<PacienteJpaEntity, Long> {

    PacienteJpaEntity findByNomeCompleto(String nomeCompleto);

    @Query("select coalesce(max(p.id), 0) + 1 from PacienteJpaEntity p")
    long proximoId();
}
