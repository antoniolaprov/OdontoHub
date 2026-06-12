package com.g4.odontohub.medicamento.infrastructure.persistence.jpa;

import com.g4.odontohub.medicamento.domain.model.Medicamento;
import com.g4.odontohub.medicamento.domain.repository.MedicamentoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link MedicamentoRepository}. */
@Repository
public class JpaMedicamentoRepository implements MedicamentoRepository {

    private final SpringDataMedicamentoRepository repository;

    public JpaMedicamentoRepository(SpringDataMedicamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Medicamento medicamento) {
        repository.save(MedicamentoJpaEntity.fromDomain(medicamento));
    }

    @Override
    public Medicamento buscarPorNome(String nomeComercial) {
        MedicamentoJpaEntity e = repository.findByNomeComercial(nomeComercial);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<Medicamento> todos() {
        return repository.findAll().stream()
                .map(MedicamentoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
