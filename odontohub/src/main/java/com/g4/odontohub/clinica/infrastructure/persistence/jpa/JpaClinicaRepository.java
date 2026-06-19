package com.g4.odontohub.clinica.infrastructure.persistence.jpa;

import com.g4.odontohub.clinica.domain.model.Clinica;
import com.g4.odontohub.clinica.domain.model.ClinicaId;
import com.g4.odontohub.clinica.domain.repository.ClinicaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link ClinicaRepository}. */
@Repository
public class JpaClinicaRepository implements ClinicaRepository {

    private final SpringDataClinicaRepository repository;

    public JpaClinicaRepository(SpringDataClinicaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Clinica clinica) {
        repository.save(ClinicaJpaEntity.fromDomain(clinica));
    }

    @Override
    public Clinica buscarPorId(ClinicaId id) {
        return repository.findById(id.id()).map(ClinicaJpaEntity::toDomain).orElse(null);
    }

    @Override
    public Clinica buscarPorEmail(String email) {
        if (email == null) return null;
        ClinicaJpaEntity e = repository.findByEmail(email);
        return e == null ? null : e.toDomain();
    }

    @Override
    public Clinica buscarPorCnpj(String cnpj) {
        if (cnpj == null) return null;
        ClinicaJpaEntity e = repository.findByCnpj(cnpj);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<Clinica> todas() {
        return repository.findAll().stream()
                .map(ClinicaJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
