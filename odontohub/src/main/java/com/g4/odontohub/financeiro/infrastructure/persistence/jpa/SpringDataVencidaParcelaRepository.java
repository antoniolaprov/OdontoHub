package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataVencidaParcelaRepository extends JpaRepository<VencidaParcelaJpaEntity, Long> {

    List<VencidaParcelaJpaEntity> findByPaciente(String paciente);
}
