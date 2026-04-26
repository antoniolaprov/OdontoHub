package com.g4.odontohub.material.domain;

public interface MaterialRepository {
    void salvar(Material material);
    Material buscarPorId(String id);
}