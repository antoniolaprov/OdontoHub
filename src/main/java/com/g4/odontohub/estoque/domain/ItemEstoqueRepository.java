package com.g4.odontohub.estoque.domain;

import java.util.List;
import java.util.Optional;

public interface ItemEstoqueRepository {
    ItemEstoque salvar(ItemEstoque item);
    Optional<ItemEstoque> buscarPorId(String id);
    Optional<ItemEstoque> buscarPorNome(String nome);
    List<ItemEstoque> listarTodos();
}
