package com.g4.odontohub.pagamento.domain.model;

import java.time.LocalDate;

public class Pagamento {

    private final PagamentoId id;
    private final String parcelaReferencia;
    private double valorRecebido;
    private LocalDate dataPagamento;
    private final FormaPagamento formaPagamento;
    private StatusPagamento status;
    private String observacao;
    private String justificativaCancelamento;

    public Pagamento(PagamentoId id, String parcelaReferencia, FormaPagamento formaPagamento, StatusPagamento status) {
        this.id = id;
        this.parcelaReferencia = parcelaReferencia;
        this.formaPagamento = formaPagamento;
        this.status = status;
    }

    public void confirmar(double valorRecebido, LocalDate dataPagamento) {
        this.valorRecebido = valorRecebido;
        this.dataPagamento = dataPagamento;
        this.status = StatusPagamento.PAGO;
    }

    public void cancelar(String justificativa) {
        this.status = StatusPagamento.CANCELADO;
        this.justificativaCancelamento = justificativa;
    }

    public boolean estaPago() {
        return status == StatusPagamento.PAGO;
    }

    public boolean estaAguardando() {
        return status == StatusPagamento.AGUARDANDO_COMPROVANTE;
    }

    public PagamentoId getId() { return id; }
    public String getParcelaReferencia() { return parcelaReferencia; }
    public double getValorRecebido() { return valorRecebido; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public StatusPagamento getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public String getJustificativaCancelamento() { return justificativaCancelamento; }

    public void setObservacao(String observacao) { this.observacao = observacao; }
}
