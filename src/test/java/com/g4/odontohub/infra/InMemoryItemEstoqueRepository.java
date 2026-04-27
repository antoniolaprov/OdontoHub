package com.g4.odontohub.infra;

import com.g4.odontohub.estoque.domain.ItemEstoque;
import com.g4.odontohub.estoque.domain.ItemEstoqueRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryItemEstoqueRepository implements ItemEstoqueRepository {

    private final Map<String, ItemEstoque> db = new HashMap<>();

    @Override
    public ItemEstoque salvar(ItemEstoque item) {
        db.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<ItemEstoque> buscarPorId(String id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public Optional<ItemEstoque> buscarPorNome(String nome) {
        return db.values().stream()
                .filter(i -> i.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    @Override
    public List<ItemEstoque> listarTodos() {
        return new ArrayList<>(db.values());
    }

    /** Remove todos os itens com o nome informado (case-insensitive). */
    public void removerPorNome(String nome) {
        db.entrySet().removeIf(e -> e.getValue().getNome().equalsIgnoreCase(nome));
    }
}
