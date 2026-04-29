package com.g4.odontohub.financeiro.domain.model;

import com.g4.odontohub.financeiro.domain.event.EntradaGeradaAutomaticamente;
import com.g4.odontohub.financeiro.domain.event.ParcelaLiquidada;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Parcela {

    private final ParcelaId id;
    private final ParcelaplanoId planoId;
    private final double valor;
    private final LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private StatusParcela status;
    private int diasAtraso;
    private double multa;
    private double juros;

    public Parcela(ParcelaId id, ParcelaplanoId planoId, double valor, LocalDate dataVencimento) {
        this.id = id;
        this.planoId = planoId;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.status = StatusParcela.PENDENTE;
    }

    public List<Object> liquidar(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
        this.status = StatusParcela.LIQUIDADA;
        List<Object> eventos = new ArrayList<>();
        eventos.add(new ParcelaLiquidada(id, valor, dataPagamento));
        eventos.add(new EntradaGeradaAutomaticamente(null, valor, dataPagamento, "Liquidação de parcela"));
        return eventos;
    }

    public ParcelaId getId() { return id; }
    public ParcelaplanoId getPlanoId() { return planoId; }
    public double getValor() { return valor; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public StatusParcela getStatus() { return status; }
    public int getDiasAtraso() { return diasAtraso; }
    public double getMulta() { return multa; }
    public double getJuros() { return juros; }
}
