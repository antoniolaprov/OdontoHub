package com.g4.odontohub.agendamento.infrastructure.persistence.jpa;

import com.g4.odontohub.agendamento.domain.model.EntradaHistorico;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class EntradaHistoricoEmbeddable {

    @Column(length = 300)
    private String acao;
    private String responsavel;
    private LocalDateTime dataRegistro;

    protected EntradaHistoricoEmbeddable() {
    }

    public static EntradaHistoricoEmbeddable fromDomain(EntradaHistorico h) {
        EntradaHistoricoEmbeddable e = new EntradaHistoricoEmbeddable();
        e.acao = h.getAcao();
        e.responsavel = h.getResponsavel();
        e.dataRegistro = h.getDataRegistro();
        return e;
    }

    public EntradaHistorico toDomain() {
        return new EntradaHistorico(acao, responsavel, dataRegistro);
    }
}
