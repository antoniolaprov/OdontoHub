package com.g4.odontohub.estoque.infrastructure.persistence.jpa;

import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.repository.InstrumentoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** Adapter JPA (infraestrutura) da porta {@link InstrumentoRepository}. */
@Repository
public class JpaInstrumentoRepository implements InstrumentoRepository {

    private final SpringDataInstrumentoRepository repository;

    public JpaInstrumentoRepository(SpringDataInstrumentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void salvar(Instrumento instrumento) {
        repository.save(InstrumentoJpaEntity.fromDomain(instrumento));
    }

    @Override
    public Instrumento buscarPorId(Long id) {
        return repository.findById(id).map(InstrumentoJpaEntity::toDomain).orElse(null);
    }

    @Override
    public Instrumento buscarPorNome(String nome) {
        InstrumentoJpaEntity e = repository.findByNome(nome);
        return e == null ? null : e.toDomain();
    }

    @Override
    public Instrumento buscarPorCodigo(String codigo) {
        InstrumentoJpaEntity e = repository.findByCodigoIdentificador(codigo);
        return e == null ? null : e.toDomain();
    }

    @Override
    public List<Instrumento> todos() {
        return repository.findAll().stream()
                .map(InstrumentoJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long proximoId() {
        return repository.proximoId();
    }
}
