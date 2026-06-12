package com.g4.odontohub.cadastropaciente.infrastructure.persistence.jpa;

import com.g4.odontohub.cadastropaciente.domain.model.AlteracaoCadastral;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class AlteracaoCadastralEmbeddable {

    private String campo;
    private String valorAnterior;
    private String valorAtualizado;
    private String responsavel;
    private LocalDate dataAlteracao;

    protected AlteracaoCadastralEmbeddable() {
    }

    public static AlteracaoCadastralEmbeddable fromDomain(AlteracaoCadastral a) {
        AlteracaoCadastralEmbeddable e = new AlteracaoCadastralEmbeddable();
        e.campo = a.campo();
        e.valorAnterior = a.valorAnterior();
        e.valorAtualizado = a.valorAtualizado();
        e.responsavel = a.responsavel();
        e.dataAlteracao = a.dataAlteracao();
        return e;
    }

    public AlteracaoCadastral toDomain() {
        return new AlteracaoCadastral(campo, valorAnterior, valorAtualizado, responsavel, dataAlteracao);
    }
}
