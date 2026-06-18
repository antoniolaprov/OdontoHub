package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.CanalContato;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.ResultadoContato;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.TentativaContato;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

/** Mapeamento de persistência de uma tentativa de contato do recall (Data Mapper). */
@Embeddable
public class TentativaContatoEmbeddable {

    private String responsavel;
    private LocalDateTime dataHora;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CanalContato canal;
    private int duracaoMinutos;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ResultadoContato resultado;
    @Column(length = 500)
    private String observacoes;

    protected TentativaContatoEmbeddable() {
    }

    static TentativaContatoEmbeddable fromDomain(TentativaContato t) {
        TentativaContatoEmbeddable e = new TentativaContatoEmbeddable();
        e.responsavel = t.responsavel();
        e.dataHora = t.dataHora();
        e.canal = t.canal();
        e.duracaoMinutos = t.duracaoMinutos();
        e.resultado = t.resultado();
        e.observacoes = t.observacoes();
        return e;
    }

    TentativaContato toDomain() {
        return new TentativaContato(responsavel, dataHora, canal, duracaoMinutos, resultado, observacoes);
    }
}
