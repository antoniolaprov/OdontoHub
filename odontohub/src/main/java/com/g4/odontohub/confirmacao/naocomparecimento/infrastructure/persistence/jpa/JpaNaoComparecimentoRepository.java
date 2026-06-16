package com.g4.odontohub.confirmacao.naocomparecimento.infrastructure.persistence.jpa;

import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimento;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.repository.NaoComparecimentoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link NaoComparecimentoRepository}. */
@Repository
public class JpaNaoComparecimentoRepository implements NaoComparecimentoRepository {

    private final SpringDataNaoComparecimentoRepository repository;

    public JpaNaoComparecimentoRepository(SpringDataNaoComparecimentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(NaoComparecimento registro) {
        repository.save(NaoComparecimentoJpaEntity.fromDomain(registro));
    }

    @Override
    public NaoComparecimento buscarPorAgendamento(Long agendamentoId) {
        NaoComparecimentoJpaEntity e = repository.findFirstByAgendamentoId(agendamentoId);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<NaoComparecimento> listarPorPaciente(String paciente) {
        return repository.findByPaciente(paciente).stream()
                .map(NaoComparecimentoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<NaoComparecimento> todos() {
        return repository.findAll().stream()
                .map(NaoComparecimentoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
