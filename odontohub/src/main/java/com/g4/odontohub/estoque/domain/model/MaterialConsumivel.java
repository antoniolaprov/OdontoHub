package com.g4.odontohub.estoque.domain.model;

import com.g4.odontohub.estoque.domain.event.ConsumoRegistrado;
import com.g4.odontohub.estoque.domain.event.EstoqueBaixoAlertado;
import com.g4.odontohub.estoque.domain.event.ReposicaoRegistrada;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MaterialConsumivel {

    private final MaterialId id;
    private final String nome;
    private final String unidadeMedida;
    private int quantidadeEmEstoque;
    private final int pontoMinimo;
    private LocalDate ultimaAtualizacao;

    public MaterialConsumivel(MaterialId id, String nome, String unidadeMedida,
                              int quantidadeEmEstoque, int pontoMinimo, LocalDate ultimaAtualizacao) {
        this.id = id;
        this.nome = nome;
        this.unidadeMedida = unidadeMedida;
        this.quantidadeEmEstoque = quantidadeEmEstoque;
        this.pontoMinimo = pontoMinimo;
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public List<Object> registrarReposicao(Reposicao reposicao) {
        this.quantidadeEmEstoque += reposicao.getQuantidade();
        this.ultimaAtualizacao = LocalDate.now();
        double valorTotal = reposicao.getQuantidade() * reposicao.getCustoUnitario();
        List<Object> eventos = new ArrayList<>();
        eventos.add(new ReposicaoRegistrada(
                reposicao.getId().id(), id.id(),
                reposicao.getQuantidade(), reposicao.getCustoUnitario(), valorTotal));
        return eventos;
    }

    public List<Object> registrarConsumo(int quantidade, Long procedimentoId) {
        this.quantidadeEmEstoque -= quantidade;
        this.ultimaAtualizacao = LocalDate.now();
        List<Object> eventos = new ArrayList<>();
        eventos.add(new ConsumoRegistrado(id.id(), quantidade, procedimentoId));
        if (quantidadeEmEstoque <= pontoMinimo) {
            eventos.add(new EstoqueBaixoAlertado(id.id(), nome, quantidadeEmEstoque, pontoMinimo));
        }
        return eventos;
    }

    public void ajustarSaldo(int novaQuantidade) {
        this.quantidadeEmEstoque = novaQuantidade;
    }

    public MaterialId getId() { return id; }
    public String getNome() { return nome; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public int getQuantidadeEmEstoque() { return quantidadeEmEstoque; }
    public int getPontoMinimo() { return pontoMinimo; }
    public LocalDate getUltimaAtualizacao() { return ultimaAtualizacao; }
}
