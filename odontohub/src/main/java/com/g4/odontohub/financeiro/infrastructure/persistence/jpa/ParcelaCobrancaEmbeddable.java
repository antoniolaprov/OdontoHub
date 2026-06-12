package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.model.StatusParcela;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ParcelaCobrancaEmbeddable {

    private double valor;
    private int diasAtraso;
    private double multa;
    private double juros;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusParcela status;

    protected ParcelaCobrancaEmbeddable() {
    }

    public static ParcelaCobrancaEmbeddable fromDomain(ParcelaCobranca p) {
        ParcelaCobrancaEmbeddable e = new ParcelaCobrancaEmbeddable();
        e.valor = p.getValor();
        e.diasAtraso = p.getDiasAtraso();
        e.multa = p.getMulta();
        e.juros = p.getJuros();
        e.status = p.getStatus();
        return e;
    }

    public ParcelaCobranca toDomain() {
        return ParcelaCobranca.reconstituir(valor, diasAtraso, multa, juros, status);
    }
}
