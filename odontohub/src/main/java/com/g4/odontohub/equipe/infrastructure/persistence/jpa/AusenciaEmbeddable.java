package com.g4.odontohub.equipe.infrastructure.persistence.jpa;

import com.g4.odontohub.equipe.domain.model.PeriodoAusencia;
import com.g4.odontohub.equipe.domain.model.TipoAusencia;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

/** Mapeamento de persistência de um período de ausência do colaborador (Data Mapper). */
@Embeddable
public class AusenciaEmbeddable {

    private LocalDate inicio;
    private LocalDate fim;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoAusencia tipo;
    private String motivo;

    protected AusenciaEmbeddable() {
    }

    static AusenciaEmbeddable fromDomain(PeriodoAusencia p) {
        AusenciaEmbeddable e = new AusenciaEmbeddable();
        e.inicio = p.inicio();
        e.fim = p.fim();
        e.tipo = p.tipo();
        e.motivo = p.motivo();
        return e;
    }

    PeriodoAusencia toDomain() {
        return new PeriodoAusencia(inicio, fim, tipo, motivo);
    }
}
