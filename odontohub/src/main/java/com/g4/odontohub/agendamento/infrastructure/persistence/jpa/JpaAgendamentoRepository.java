package com.g4.odontohub.agendamento.infrastructure.persistence.jpa;

import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.AgendamentoId;
import com.g4.odontohub.agendamento.domain.repository.AgendamentoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link AgendamentoRepository}. */
@Repository
public class JpaAgendamentoRepository implements AgendamentoRepository {

    private final SpringDataAgendamentoRepository repository;

    public JpaAgendamentoRepository(SpringDataAgendamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Agendamento agendamento) {
        repository.save(AgendamentoJpaEntity.fromDomain(agendamento));
    }

    @Override
    public Agendamento buscarPorId(AgendamentoId id) {
        return repository.findById(id.id()).map(AgendamentoJpaEntity::toDomain).orElse(null);
    }

    @Override
    public List<Agendamento> todos() {
        return repository.findAll().stream()
                .map(AgendamentoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
