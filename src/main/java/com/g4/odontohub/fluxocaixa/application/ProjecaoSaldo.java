package com.g4.odontohub.fluxocaixa.application;

import java.math.BigDecimal;

/**
 * Value object com o resultado da projeção de saldo do fluxo de caixa.
 * AUDITORIA: Java puro, sem anotações Spring.
 */
public class ProjecaoSaldo {

    private final BigDecimal entradas;
    private final BigDecimal saidas;
    private final BigDecimal saldo;

    public ProjecaoSaldo(BigDecimal entradas, BigDecimal saidas, BigDecimal saldo) {
        this.entradas = entradas;
        this.saidas = saidas;
        this.saldo = saldo;
    }

    public BigDecimal getEntradas() { return entradas; }
    public BigDecimal getSaidas()   { return saidas; }
    public BigDecimal getSaldo()    { return saldo; }
}
