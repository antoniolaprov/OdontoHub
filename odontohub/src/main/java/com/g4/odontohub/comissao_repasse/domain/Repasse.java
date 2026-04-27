package com.g4.odontohub.comissao_repasse.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Repasse {

    private UUID id;
    private String nomeEspecialista;
    private BigDecimal valor;
    private LocalDate data;
    private boolean estornado;
    private String justificativaEstorno;

    private Repasse() {}

    public static Repasse criar(String nomeEspecialista, BigDecimal valor, LocalDate data) {
        Repasse r = new Repasse();
        r.id = UUID.randomUUID();
        r.nomeEspecialista = nomeEspecialista;
        r.valor = valor;
        r.data = data;
        r.estornado = false;
        return r;
    }

    public void excluir() {
        throw new DomainException("Repasses registrados não podem ser excluídos, apenas estornados");
    }

    public void estornar(String justificativa) {
        this.estornado = true;
        this.justificativaEstorno = justificativa;
    }
}