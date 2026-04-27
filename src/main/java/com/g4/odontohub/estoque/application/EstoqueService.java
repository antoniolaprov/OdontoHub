package com.g4.odontohub.estoque.application;

import com.g4.odontohub.estoque.domain.ItemEstoque;
import com.g4.odontohub.estoque.domain.ItemEstoqueRepository;
import com.g4.odontohub.shared.exception.DomainException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço de aplicação do contexto de Estoque.
 * Orquestra as regras de negócio delegando ao domínio e ao repositório.
 * AUDITORIA: Sem @Service, @Component ou qualquer anotação Spring aqui.
 */
public class EstoqueService {

    private final ItemEstoqueRepository repository;

    public EstoqueService(ItemEstoqueRepository repository) {
        this.repository = repository;
    }

    // ── Casos de uso ─────────────────────────────────────────────────────────

    /** Cadastra um item no estoque. Retorna o ID gerado. */
    public String cadastrar(String nome, int saldoInicial, Integer pontoMinimo) {
        ItemEstoque item = ItemEstoque.criar(nome, saldoInicial, pontoMinimo);
        repository.salvar(item);
        return item.getId();
    }

    /**
     * Realiza baixa de múltiplos materiais ao confirmar um procedimento.
     * Valida estoque suficiente para TODOS antes de baixar qualquer um.
     * Retorna mapa de nomes de materiais para boolean (true = alerta de mínimo emitido).
     */
    public Map<String, Boolean> realizarProcedimento(Map<String, Integer> consumoPorMaterial) {
        // Primeiro: valida todos
        for (Map.Entry<String, Integer> entry : consumoPorMaterial.entrySet()) {
            String nome = entry.getKey();
            int qtd = entry.getValue();
            ItemEstoque item = buscarPorNomeOuFalhar(nome);
            if (item.getSaldo() < qtd) {
                throw new DomainException(
                        "Estoque insuficiente de " + nome + ": disponível " + item.getSaldo() + ", necessário " + qtd);
            }
        }

        // Depois: executa as baixas
        Map<String, Boolean> alertas = new HashMap<>();
        for (Map.Entry<String, Integer> entry : consumoPorMaterial.entrySet()) {
            String nome = entry.getKey();
            int qtd = entry.getValue();
            ItemEstoque item = buscarPorNomeOuFalhar(nome);
            boolean alerta = item.baixar(qtd);
            repository.salvar(item);
            alertas.put(nome, alerta);
        }
        return alertas;
    }

    /** Define o ponto mínimo de um material. */
    public void definirPontoMinimo(String id, int pontoMinimo) {
        ItemEstoque item = repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Item de estoque não encontrado: " + id));
        item.definirPontoMinimo(pontoMinimo);
        repository.salvar(item);
    }

    /** Define o ponto mínimo pelo nome do material. */
    public void definirPontoMinimoPorNome(String nome, int pontoMinimo) {
        ItemEstoque item = buscarPorNomeOuFalhar(nome);
        item.definirPontoMinimo(pontoMinimo);
        repository.salvar(item);
    }

    public ItemEstoque buscarPorId(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Item de estoque não encontrado: " + id));
    }

    public ItemEstoque buscarPorNome(String nome) {
        return buscarPorNomeOuFalhar(nome);
    }

    public List<ItemEstoque> listarTodos() {
        return repository.listarTodos();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ItemEstoque buscarPorNomeOuFalhar(String nome) {
        return repository.buscarPorNome(nome)
                .orElseThrow(() -> new DomainException("Material não encontrado: " + nome));
    }
}
