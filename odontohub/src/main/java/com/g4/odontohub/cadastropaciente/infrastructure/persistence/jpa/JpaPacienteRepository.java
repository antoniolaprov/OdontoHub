package com.g4.odontohub.cadastropaciente.infrastructure.persistence.jpa;

import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;
import com.g4.odontohub.cadastropaciente.domain.repository.PacienteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link PacienteRepository}. */
@Repository
public class JpaPacienteRepository implements PacienteRepository {

    private final SpringDataPacienteRepository repository;

    public JpaPacienteRepository(SpringDataPacienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Paciente paciente) {
        repository.save(PacienteJpaEntity.fromDomain(paciente));
    }

    @Override
    public Paciente buscarPorId(PacienteRegistroId id) {
        return repository.findById(id.id()).map(PacienteJpaEntity::toDomain).orElse(null);
    }

    @Override
    public Paciente buscarPorNome(String nomeCompleto) {
        PacienteJpaEntity e = repository.findByNomeCompleto(nomeCompleto);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<Paciente> todos() {
        return repository.findAll().stream()
                .map(PacienteJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
