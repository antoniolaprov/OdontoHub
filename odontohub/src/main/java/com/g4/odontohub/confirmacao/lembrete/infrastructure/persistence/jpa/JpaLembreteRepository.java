package com.g4.odontohub.confirmacao.lembrete.infrastructure.persistence.jpa;

import com.g4.odontohub.confirmacao.lembrete.domain.model.Lembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.model.LembreteId;
import com.g4.odontohub.confirmacao.lembrete.domain.repository.LembreteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link LembreteRepository}. */
@Repository
public class JpaLembreteRepository implements LembreteRepository {

    private final SpringDataLembreteRepository repository;

    public JpaLembreteRepository(SpringDataLembreteRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Lembrete lembrete) {
        repository.save(LembreteJpaEntity.fromDomain(lembrete));
    }

    @Override
    public Lembrete buscarPorId(LembreteId id) {
        return repository.findById(id.id()).map(LembreteJpaEntity::toDomain).orElse(null);
    }

    @Override
    public Lembrete buscarPorAgendamento(Long agendamentoId) {
        LembreteJpaEntity e = repository.findFirstByAgendamentoIdOrderByIdDesc(agendamentoId);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<Lembrete> todos() {
        return repository.findAll().stream()
                .map(LembreteJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
