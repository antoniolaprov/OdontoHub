package com.g4.odontohub.relacionamentopaciente.followup.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupPosOperatorio;
import com.g4.odontohub.relacionamentopaciente.followup.domain.repository.FollowupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link FollowupRepository}. */
@Repository
public class JpaFollowupRepository implements FollowupRepository {

    private final SpringDataFollowupRepository repository;

    public JpaFollowupRepository(SpringDataFollowupRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(FollowupPosOperatorio tarefa) {
        repository.save(FollowupJpaEntity.fromDomain(tarefa));
    }

    @Override
    public List<FollowupPosOperatorio> todos() {
        return repository.findAll().stream()
                .map(FollowupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
