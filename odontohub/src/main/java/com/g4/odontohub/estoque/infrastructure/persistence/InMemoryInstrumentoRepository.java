package com.g4.odontohub.estoque.infrastructure.persistence;

import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.repository.InstrumentoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryInstrumentoRepository implements InstrumentoRepository {

    private final Map<Long, Instrumento> instrumentos = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Instrumento instrumento) {
        instrumentos.put(instrumento.getId().id(), instrumento);
    }

    @Override
    public Instrumento buscarPorId(Long id) {
        return instrumentos.get(id);
    }

    @Override
    public Instrumento buscarPorNome(String nome) {
        return instrumentos.values().stream()
                .filter(i -> i.getNome().equals(nome))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Instrumento buscarPorCodigo(String codigo) {
        return instrumentos.values().stream()
                .filter(i -> i.getCodigoIdentificador().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Instrumento> todos() {
        return new ArrayList<>(instrumentos.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
