package com.g4.odontohub.equipe.infrastructure.persistence.jpa;

import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.repository.ColaboradorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link ColaboradorRepository}. */
@Repository
public class JpaColaboradorRepository implements ColaboradorRepository {

    private final SpringDataColaboradorRepository repository;

    public JpaColaboradorRepository(SpringDataColaboradorRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Colaborador colaborador) {
        repository.save(ColaboradorJpaEntity.fromDomain(colaborador));
    }

    @Override
    public Colaborador buscarPorNome(String nomeCompleto) {
        ColaboradorJpaEntity e = repository.findByNomeCompleto(nomeCompleto);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<Colaborador> todos() {
        return repository.findAll().stream()
                .map(ColaboradorJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
