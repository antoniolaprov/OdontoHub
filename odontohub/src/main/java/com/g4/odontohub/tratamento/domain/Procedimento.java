package com.g4.odontohub.tratamento.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

@Getter
public class Procedimento {
    private Long id;
    private String descricao;
    private StatusProcedimento status;
    private String justificativaCancelamento;

    private Procedimento() {}

    public static Procedimento criar(Long id, String descricao) {
        Procedimento p = new Procedimento();
        p.id = id;
        p.descricao = descricao;
        p.status = StatusProcedimento.PENDENTE;
        return p;
    }

    public void cancelar(String justificativa) {
        if (justificativa == null || justificativa.trim().isEmpty()) {
            throw new DomainException("Justificativa é obrigatória para cancelar um procedimento");
        }
        this.justificativaCancelamento = justificativa;
        this.status = StatusProcedimento.CANCELADO;
    }

    public void realizar() {
        this.status = StatusProcedimento.REALIZADO;
    }
}