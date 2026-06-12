package com.g4.odontohub.prontuarioclinico.infrastructure.persistence.jpa;

import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.LogAuditoria;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Embeddable
public class LogAuditoriaEmbeddable {

    private String acao;
    private String justificativa;
    private String responsavel;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataAcao;

    protected LogAuditoriaEmbeddable() {
    }

    public static LogAuditoriaEmbeddable fromDomain(LogAuditoria l) {
        LogAuditoriaEmbeddable e = new LogAuditoriaEmbeddable();
        e.acao = l.acao();
        e.justificativa = l.justificativa();
        e.responsavel = l.responsavel();
        e.dataAcao = l.dataAcao();
        return e;
    }

    public LogAuditoria toDomain() {
        return new LogAuditoria(acao, justificativa, responsavel, dataAcao);
    }
}
