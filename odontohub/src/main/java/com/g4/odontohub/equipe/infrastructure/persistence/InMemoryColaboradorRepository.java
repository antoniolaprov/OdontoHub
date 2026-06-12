package com.g4.odontohub.equipe.infrastructure.persistence;

import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.repository.ColaboradorRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryColaboradorRepository implements ColaboradorRepository {

    private final Map<Long, Colaborador> colaboradores = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Colaborador colaborador) {
        colaboradores.put(colaborador.getId().id(), colaborador);
    }

    @Override
    public Colaborador buscarPorNome(String nomeCompleto) {
        return colaboradores.values().stream()
                .filter(c -> c.getNomeCompleto().equals(nomeCompleto))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Colaborador> todos() {
        return new ArrayList<>(colaboradores.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
