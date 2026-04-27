package com.g4.odontohub.inadimplencia.domain;

import lombok.Getter;

import java.time.LocalDate;

/**
 * Entidade de domínio puro: Parcela de pagamento parcelado.
 * private constructor + static factory criar(). Apenas @Getter.
 */
@Getter
public class ParcelaInadimplencia {

    private int numero;
    private LocalDate dataVencimento;
    private StatusParcela status;
    private LocalDate dataPagamento;

    private ParcelaInadimplencia() {}

    public static ParcelaInadimplencia criar(int numero, LocalDate dataVencimento, StatusParcela status) {
        ParcelaInadimplencia p = new ParcelaInadimplencia();
        p.numero = numero;
        p.dataVencimento = dataVencimento;
        p.status = status;
        return p;
    }

    /**
     * Marca a parcela como INADIMPLENTE quando a data de vencimento passou.
     */
    public void marcarInadimplente() {
        this.status = StatusParcela.INADIMPLENTE;
    }

    /**
     * Quita a parcela, registrando a data de pagamento.
     */
    public void liquidar(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
        this.status = StatusParcela.LIQUIDADA;
    }

    public boolean estaInadimplente() {
        return this.status == StatusParcela.INADIMPLENTE;
    }
}
