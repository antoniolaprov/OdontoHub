package com.g4.odontohub.pagamento.domain.model;

public class ParcelaPagavel {

    private final String referencia;
    private final double valorDevido;
    private double valorPago;
    private StatusParcelaPagamento status;

    public ParcelaPagavel(String referencia, double valorDevido) {
        this.referencia = referencia;
        this.valorDevido = valorDevido;
        this.status = StatusParcelaPagamento.EM_ABERTO;
    }

    public void registrarPagamento(double valor) {
        this.valorPago += valor;
        if (valorPago >= valorDevido) {
            this.status = StatusParcelaPagamento.PAGA;
        } else {
            this.status = StatusParcelaPagamento.PARCIALMENTE_PAGA;
        }
    }

    public double getSaldoRemanescente() {
        double saldo = valorDevido - valorPago;
        return saldo > 0 ? saldo : 0;
    }

    public boolean estaQuitada() {
        return status == StatusParcelaPagamento.PAGA;
    }

    public String getReferencia() { return referencia; }
    public double getValorDevido() { return valorDevido; }
    public double getValorPago() { return valorPago; }
    public StatusParcelaPagamento getStatus() { return status; }
}
