package com.g4.odontohub.estoque.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.util.UUID;

/**
 * Aggregate root do contexto de Estoque.
 * AUDITORIA: Java puro — sem @Entity, @Table, @Id ou qualquer anotação JPA/Spring.
 * Apenas @Getter. Comportamentos expressos como métodos de negócio.
 */
@Getter
public class ItemEstoque {

    private String id;
    private String nome;
    private int saldo;
    /** Ponto mínimo abaixo do qual deve ser emitido alerta. Null = sem ponto definido. */
    private Integer pontoMinimo;

    private ItemEstoque() {}

    // ── Fábrica ──────────────────────────────────────────────────────────────

    public static ItemEstoque criar(String nome, int saldoInicial, Integer pontoMinimo) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("O nome do material é obrigatório");
        }
        if (saldoInicial < 0) {
            throw new DomainException("O saldo inicial não pode ser negativo");
        }

        ItemEstoque i = new ItemEstoque();
        i.id = UUID.randomUUID().toString();
        i.nome = nome;
        i.saldo = saldoInicial;
        i.pontoMinimo = pontoMinimo;
        return i;
    }

    // ── Comportamentos ───────────────────────────────────────────────────────

    /**
     * Realiza baixa no estoque. Lança DomainException se o saldo for insuficiente.
     * Retorna true se o saldo resultante ficou abaixo do pontoMinimo.
     */
    public boolean baixar(int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("A quantidade para baixa deve ser maior que zero");
        }
        if (this.saldo < quantidade) {
            throw new DomainException(
                    "Estoque insuficiente de " + nome + ": disponível " + saldo + ", necessário " + quantidade);
        }
        this.saldo -= quantidade;
        return pontoMinimo != null && this.saldo < pontoMinimo;
    }

    /**
     * Define ou redefine o ponto mínimo de estoque.
     */
    public void definirPontoMinimo(int pontoMinimo) {
        if (pontoMinimo < 0) {
            throw new DomainException("O ponto mínimo não pode ser negativo");
        }
        this.pontoMinimo = pontoMinimo;
    }
}
