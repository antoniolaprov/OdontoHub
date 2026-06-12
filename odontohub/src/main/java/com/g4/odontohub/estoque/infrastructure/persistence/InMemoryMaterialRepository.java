package com.g4.odontohub.estoque.infrastructure.persistence;

import com.g4.odontohub.estoque.domain.model.MaterialConsumivel;
import com.g4.odontohub.estoque.domain.repository.MaterialRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryMaterialRepository implements MaterialRepository {

    private final Map<String, MaterialConsumivel> materiais = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(MaterialConsumivel material) {
        materiais.put(material.getNome(), material);
    }

    @Override
    public MaterialConsumivel buscarPorNome(String nome) {
        return materiais.get(nome);
    }

    @Override
    public List<MaterialConsumivel> todos() {
        return new ArrayList<>(materiais.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
