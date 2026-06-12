package com.g4.odontohub.relacionamentopaciente.churn.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.churn.domain.model.AnaliseChurn;
import com.g4.odontohub.relacionamentopaciente.churn.domain.repository.ChurnRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link ChurnRepository}. */
@Repository
public class JpaChurnRepository implements ChurnRepository {

    private final SpringDataChurnRepository repository;

    public JpaChurnRepository(SpringDataChurnRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(AnaliseChurn analise) {
        repository.save(AnaliseChurnJpaEntity.fromDomain(analise));
    }

    @Override
    public AnaliseChurn buscarPorPaciente(String paciente) {
        AnaliseChurnJpaEntity e = repository.findByPaciente(paciente);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<AnaliseChurn> todos() {
        return repository.findAll().stream()
                .map(AnaliseChurnJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
