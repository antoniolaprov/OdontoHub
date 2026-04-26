package com.g4.odontohub.infra;

import com.g4.odontohub.material.domain.Material;
import com.g4.odontohub.material.domain.MaterialRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryMaterialRepository implements MaterialRepository {
    private final Map<String, Material> db = new HashMap<>();

    @Override
    public void salvar(Material material) {
        db.put(material.getId(), material);
    }

    @Override
    public Material buscarPorId(String id) {
        return db.get(id);
    }
}