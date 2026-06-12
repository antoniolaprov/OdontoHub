package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link RecallRepository}. */
@Repository
public class JpaRecallRepository implements RecallRepository {

    private final SpringDataRecallRepository repository;

    public JpaRecallRepository(SpringDataRecallRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Recall recall) {
        repository.save(RecallJpaEntity.fromDomain(recall));
    }

    @Override
    public List<Recall> todos() {
        return repository.findAll().stream()
                .map(RecallJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
