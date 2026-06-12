package com.g4.odontohub.estoque.infrastructure.persistence.jpa;

import com.g4.odontohub.estoque.domain.model.MaterialConsumivel;
import com.g4.odontohub.estoque.domain.repository.MaterialRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link MaterialRepository}. */
@Repository
public class JpaMaterialRepository implements MaterialRepository {

    private final SpringDataMaterialRepository repository;

    public JpaMaterialRepository(SpringDataMaterialRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(MaterialConsumivel material) {
        repository.save(MaterialConsumivelJpaEntity.fromDomain(material));
    }

    @Override
    public MaterialConsumivel buscarPorNome(String nome) {
        MaterialConsumivelJpaEntity e = repository.findByNome(nome);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<MaterialConsumivel> todos() {
        return repository.findAll().stream()
                .map(MaterialConsumivelJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
