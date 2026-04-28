package com.g4.odontohub.financeiro.domain.model;

import java.time.LocalDate;

public class LancamentoFinanceiro {

    private final LancamentoId id;
    private final TipoLancamento tipo;
    private double valor;
    private final LocalDate data;
    private String categoria;
    private String descricao;
    private final boolean geradoAutomaticamente;

    public LancamentoFinanceiro(LancamentoId id, TipoLancamento tipo, double valor,
                                LocalDate data, String categoria, String descricao,
                                boolean geradoAutomaticamente) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
        this.descricao = descricao;
        this.geradoAutomaticamente = geradoAutomaticamente;
    }

    public void editar(double novoValor, String novaCategoria, String novaDescricao) {
        if (geradoAutomaticamente) {
            throw new IllegalStateException(
                    "Lançamentos gerados automaticamente não podem ser editados manualmente.");
        }
        this.valor = novoValor;
        this.categoria = novaCategoria;
        this.descricao = novaDescricao;
    }

    public LancamentoId getId() { return id; }
    public TipoLancamento getTipo() { return tipo; }
    public double getValor() { return valor; }
    public LocalDate getData() { return data; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public boolean isGeradoAutomaticamente() { return geradoAutomaticamente; }
}
