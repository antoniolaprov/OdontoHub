package com.g4.odontohub.comissao_repasse.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class Comissao {

    private UUID id;
    private String nomeEspecialista;
    private String nomeProcedimento;
    private BigDecimal valor;
    private StatusComissao status;

    private Comissao() {}

    public static Comissao criar(String nomeEspecialista, String nomeProcedimento, BigDecimal valor) {
        Comissao c = new Comissao();
        c.id = UUID.randomUUID();
        c.nomeEspecialista = nomeEspecialista;
        c.nomeProcedimento = nomeProcedimento;
        c.valor = valor;
        c.status = StatusComissao.PENDENTE;
        return c;
    }

    public void liberar() {
        this.status = StatusComissao.LIBERADA;
    }

    public void verificarPodeRepassar() {
        if (this.status != StatusComissao.LIBERADA) {
            throw new DomainException("O repasse só pode ser registrado após o pagamento do paciente");
        }
    }

    public void marcarComoRepassada() {
        this.status = StatusComissao.REPASSADA;
    }
}
