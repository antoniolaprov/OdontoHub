package com.g4.odontohub.equipe.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataColaboradorRepository extends JpaRepository<ColaboradorJpaEntity, Long> {

    ColaboradorJpaEntity findByNomeCompleto(String nomeCompleto);

    @Query("select coalesce(max(c.id), 0) + 1 from ColaboradorJpaEntity c")
    long proximoId();
}
