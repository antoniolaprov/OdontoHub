package com.g4.odontohub.prontuarioclinico.infrastructure.persistence.jpa;

import com.g4.odontohub.prontuarioclinico.application.AnamneseRepository;
import com.g4.odontohub.prontuarioclinico.domain.model.Anamnese;
import org.springframework.stereotype.Repository;

/** Adapter JPA (infraestrutura) da porta {@link AnamneseRepository}. */
@Repository
public class JpaAnamneseRepository implements AnamneseRepository {

    private final SpringDataAnamneseRepository repository;

    public JpaAnamneseRepository(SpringDataAnamneseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Anamnese anamnese) {
        repository.save(AnamneseJpaEntity.fromDomain(anamnese));
    }

    @Override
    public Anamnese buscarPorPacienteId(Long pacienteId) {
        return repository.findById(pacienteId).map(AnamneseJpaEntity::toDomain).orElse(null);
    }
}
