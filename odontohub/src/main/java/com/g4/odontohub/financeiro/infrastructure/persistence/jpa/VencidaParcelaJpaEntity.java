package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parcela_vencida")
public class VencidaParcelaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paciente;
    @Embedded
    private ParcelaCobrancaEmbeddable parcela;

    protected VencidaParcelaJpaEntity() {
    }

    public VencidaParcelaJpaEntity(String paciente, ParcelaCobranca parcela) {
        this.paciente = paciente;
        this.parcela = ParcelaCobrancaEmbeddable.fromDomain(parcela);
    }

    public ParcelaCobranca toDomain() {
        return parcela.toDomain();
    }

    public String getPaciente() {
        return paciente;
    }
}
