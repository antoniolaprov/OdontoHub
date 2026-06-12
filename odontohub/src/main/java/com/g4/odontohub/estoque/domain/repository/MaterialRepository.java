package com.g4.odontohub.estoque.domain.repository;

import com.g4.odontohub.estoque.domain.model.MaterialConsumivel;

import java.util.List;

/** Porta de persistência de materiais consumíveis (camada de domínio). */
public interface MaterialRepository {

    void salvar(MaterialConsumivel material);

    MaterialConsumivel buscarPorNome(String nome);

    List<MaterialConsumivel> todos();

    long proximoId();
}
