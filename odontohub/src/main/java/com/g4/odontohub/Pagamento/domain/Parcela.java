package com.g4.odontohub.Pagamento.domain;

import lombok.Getter;

@Getter
public class Parcela {
    private int numero;
    private double valor;
    private String status;
    private String dataVencimento;
    private String dataPagamento;

    private Parcela() {}

    public static Parcela criar(int numero, double valor, String status, String dataVencimento) {
        Parcela parcela = new Parcela();
        parcela.numero = numero;
        parcela.valor = valor;
        parcela.status = status;
        parcela.dataVencimento = dataVencimento;
        return parcela;
    }

    public void liquidar(String dataPagamento) {
        this.status = "LIQUIDADA";
        this.dataPagamento = dataPagamento;
    }
}