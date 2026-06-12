package com.g4.odontohub.prontuarioclinico.infrastructure.persistence.jpa;

import com.g4.odontohub.prontuarioclinico.application.PlanoTratamentoRepository;
import com.g4.odontohub.prontuarioclinico.domain.model.PlanoTratamento;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.PlanoId;
import org.springframework.stereotype.Repository;

/** Adapter JPA (infraestrutura) da porta {@link PlanoTratamentoRepository}. */
@Repository
public class JpaPlanoTratamentoRepository implements PlanoTratamentoRepository {

    private final SpringDataPlanoTratamentoRepository repository;

    public JpaPlanoTratamentoRepository(SpringDataPlanoTratamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(PlanoTratamento plano) {
        repository.save(PlanoTratamentoJpaEntity.fromDomain(plano));
    }

    @Override
    public PlanoTratamento buscarPorId(PlanoId id) {
        return repository.findById(id.id()).map(PlanoTratamentoJpaEntity::toDomain).orElse(null);
    }

    @Override
    public PlanoTratamento buscarPorPacienteId(Long pacienteId) {
        PlanoTratamentoJpaEntity e = repository.findFirstByPacienteId(pacienteId);
        return e == null ? null : e.toDomain();
    }

    @Override
    public void remover(PlanoId id) {
        repository.deleteById(id.id());
    }
}
