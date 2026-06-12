package com.g4.odontohub.estoque.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataMaterialRepository extends JpaRepository<MaterialConsumivelJpaEntity, Long> {

    MaterialConsumivelJpaEntity findByNome(String nome);

    @Query("select coalesce(max(m.id), 0) + 1 from MaterialConsumivelJpaEntity m")
    long proximoId();
}
