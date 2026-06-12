package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataAcordoRepository extends JpaRepository<AcordoJpaEntity, Long> {

    AcordoJpaEntity findFirstByPacienteOrderByIdDesc(String paciente);

    @Query("select coalesce(max(a.id), 0) + 1 from AcordoJpaEntity a")
    long proximoId();
}
