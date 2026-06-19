package com.g4.odontohub.financeiro.domain.model;

public class ParcelaCobranca {

    private final double valor;
    private int diasAtraso;
    private double multa;
    private double juros;
    private StatusParcela status;

    public ParcelaCobranca(double valor, int diasAtraso, double multa, double juros) {
        this.valor = valor;
        this.diasAtraso = diasAtraso;
        this.multa = multa;
        this.juros = juros;
        this.status = StatusParcela.VENCIDA;
    }

    public void editarJurosOuMulta(double novoValor) {
        throw new IllegalStateException(
                "Juros e multa sao calculados automaticamente e nao podem ser editados manualmente");
    }

    public static ParcelaCobranca reconstituir(double valor, int diasAtraso, double multa,
                                               double juros, StatusParcela status) {
        ParcelaCobranca p = new ParcelaCobranca(valor, diasAtraso, multa, juros);
        p.status = status;
        return p;
    }

    public void substituir() {
        this.status = StatusParcela.SUBSTITUIDA;
    }

    public void vencer(int diasAtraso, double multa, double juros) {
        this.diasAtraso = diasAtraso;
        this.multa = multa;
        this.juros = juros;
        this.status = StatusParcela.VENCIDA;
    }

    public void marcarPendente() {
        this.status = StatusParcela.PENDENTE;
    }

    public void cancelar() {
        this.status = StatusParcela.CANCELADA;
    }

    public void liquidar() {
        this.status = StatusParcela.LIQUIDADA;
    }

    public boolean contaNoFluxoCaixa() {
        return status != StatusParcela.SUBSTITUIDA;
    }

    public double getValorAtualizado() { return valor + multa + juros; }
    public double getValor() { return valor; }
    public int getDiasAtraso() { return diasAtraso; }
    public double getMulta() { return multa; }
    public double getJuros() { return juros; }
    public StatusParcela getStatus() { return status; }
}
