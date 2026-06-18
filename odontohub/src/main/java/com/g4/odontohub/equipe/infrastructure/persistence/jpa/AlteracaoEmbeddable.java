package com.g4.odontohub.equipe.infrastructure.persistence.jpa;

import com.g4.odontohub.equipe.domain.model.AlteracaoCadastral;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/** Mapeamento de persistência de uma alteração cadastral do colaborador (Data Mapper). */
@Embeddable
public class AlteracaoEmbeddable {

    @Column(name = "campo_alterado", length = 60)
    private String campo;
    private String valorAnterior;
    private String valorAtualizado;
    private String responsavel;
    private LocalDateTime data;

    protected AlteracaoEmbeddable() {
    }

    static AlteracaoEmbeddable fromDomain(AlteracaoCadastral a) {
        AlteracaoEmbeddable e = new AlteracaoEmbeddable();
        e.campo = a.campo();
        e.valorAnterior = a.valorAnterior();
        e.valorAtualizado = a.valorAtualizado();
        e.responsavel = a.responsavel();
        e.data = a.data();
        return e;
    }

    AlteracaoCadastral toDomain() {
        return new AlteracaoCadastral(campo, valorAnterior, valorAtualizado, responsavel, data);
    }
}
