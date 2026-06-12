package com.g4.odontohub.pagamento.infrastructure.persistence.jpa;

import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;
import com.g4.odontohub.pagamento.domain.model.StatusParcelaPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parcela_pagavel")
public class ParcelaPagavelJpaEntity {

    @Id
    private String referencia;
    private double valorDevido;
    private double valorPago;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusParcelaPagamento status;

    protected ParcelaPagavelJpaEntity() {
    }

    public static ParcelaPagavelJpaEntity fromDomain(ParcelaPagavel parcela) {
        ParcelaPagavelJpaEntity e = new ParcelaPagavelJpaEntity();
        e.referencia = parcela.getReferencia();
        e.valorDevido = parcela.getValorDevido();
        e.valorPago = parcela.getValorPago();
        e.status = parcela.getStatus();
        return e;
    }

    public ParcelaPagavel toDomain() {
        return ParcelaPagavel.reconstituir(referencia, valorDevido, valorPago, status);
    }
}
