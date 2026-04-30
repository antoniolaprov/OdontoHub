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

    // Construtor alternativo para Application Service (aceita tipos primitivos)
    public Parcela(long id, Long pacienteId, long planoId, double valor, LocalDate dataVencimento) {
        this.id = new ParcelaId(id);
        this.planoId = new ParcelaplanoId(planoId);
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.status = StatusParcela.PENDENTE;
    }

    /**
     * Calcula juros e multa com base nos dias de atraso.
     * Multa: 2% ao mês (proporcional aos dias)
     * Juros: 0,033% ao dia (1% ao mês)
     */
    public void calcularJurosEMulta() {
        this.diasAtraso = calcularDiasAtraso();

        if (diasAtraso > 0) {
            // Multa: 2% ao mês proporcional aos dias
            this.multa = valor * 0.02 * (diasAtraso / 30.0);
            // Juros: 0,033% ao dia (1% ao mês / 30)
            this.juros = valor * 0.00033 * diasAtraso;
        }
    }

    /**
     * Calcula os dias de atraso desde o vencimento.
     */
    public int calcularDiasAtraso() {
        if (dataVencimento == null)
            return 0;
        LocalDate hoje = LocalDate.now();
        if (hoje.isAfter(dataVencimento)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dataVencimento, hoje);
        }
        return 0;
    }

    /**
     * Verifica se o paciente deve ser restrito (>30 dias de atraso).
     */
    public boolean deveRestringirPaciente() {
        return calcularDiasAtraso() > 30;
    }

    /**
     * Marca a parcela como substituída por um acordo.
     */
    public void substituir() {
        this.status = StatusParcela.SUBSTITUIDA;
    }

    public List<Object> liquidar(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
        this.status = StatusParcela.LIQUIDADA;
        List<Object> eventos = new ArrayList<>();
        eventos.add(new ParcelaLiquidada(id, valor, dataPagamento));
        eventos.add(new EntradaGeradaAutomaticamente(null, valor, dataPagamento, "Liquidação de parcela"));
        return eventos;
    }

    public ParcelaId getId() {
        return id;
    }

    public ParcelaplanoId getPlanoId() {
        return planoId;
    }

    public double getValor() {
        return valor;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public StatusParcela getStatus() {
        return status;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public double getMulta() {
        return multa;
    }

    public double getJuros() {
        return juros;
    }
}
