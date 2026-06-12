package com.g4.odontohub.prontuarioclinico.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPlanoTratamentoRepository extends JpaRepository<PlanoTratamentoJpaEntity, Long> {

    PlanoTratamentoJpaEntity findFirstByPacienteId(Long pacienteId);
}
