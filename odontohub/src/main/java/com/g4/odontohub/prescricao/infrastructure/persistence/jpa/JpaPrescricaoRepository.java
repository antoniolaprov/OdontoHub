package com.g4.odontohub.prescricao.infrastructure.persistence.jpa;

import com.g4.odontohub.prescricao.domain.model.Prescricao;
import com.g4.odontohub.prescricao.domain.repository.PrescricaoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link PrescricaoRepository}. */
@Repository
public class JpaPrescricaoRepository implements PrescricaoRepository {

    private final SpringDataPrescricaoRepository repository;

    public JpaPrescricaoRepository(SpringDataPrescricaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Prescricao prescricao) {
        repository.save(PrescricaoJpaEntity.fromDomain(prescricao));
    }

    @Override
    public Prescricao buscar(Long id) {
        return repository.findById(id).map(PrescricaoJpaEntity::toDomain).orElse(null);
    }

    @Override
    public List<Prescricao> todos() {
        return repository.findAll().stream()
                .map(PrescricaoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
