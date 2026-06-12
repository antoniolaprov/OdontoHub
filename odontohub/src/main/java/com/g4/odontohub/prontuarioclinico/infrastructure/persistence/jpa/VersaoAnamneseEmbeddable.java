package com.g4.odontohub.prontuarioclinico.infrastructure.persistence.jpa;

import com.g4.odontohub.prontuarioclinico.domain.model.VersaoAnamnese;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Embeddable
public class VersaoAnamneseEmbeddable {

    private int versao;
    private String alergias;
    private String contraindicacoes;
    private String condicoesSistemicas;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataAlteracao;
    private String responsavel;

    protected VersaoAnamneseEmbeddable() {
    }

    public static VersaoAnamneseEmbeddable fromDomain(VersaoAnamnese v) {
        VersaoAnamneseEmbeddable e = new VersaoAnamneseEmbeddable();
        e.versao = v.versao();
        e.alergias = v.alergias();
        e.contraindicacoes = v.contraindicacoes();
        e.condicoesSistemicas = v.condicoesSistemicas();
        e.dataAlteracao = v.dataAlteracao();
        e.responsavel = v.responsavel();
        return e;
    }

    public VersaoAnamnese toDomain() {
        return new VersaoAnamnese(versao, alergias, contraindicacoes, condicoesSistemicas, dataAlteracao, responsavel);
    }
}
