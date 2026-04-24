package com.g4.odontohub.instrumental.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class Instrumento {
    private Long id;
    private String nome;
    private StatusEsterilizacao status;
    private LocalDate validadeEsterilizacao;

    private Instrumento() {}

    public static Instrumento criar(Long id, String nome) {
        Instrumento i = new Instrumento();
        i.id = id;
        i.nome = nome;
        i.status = StatusEsterilizacao.PENDENTE_ESTERILIZACAO;
        return i;
    }

    public void vincularProcedimento() {
        if (this.status == StatusEsterilizacao.PENDENTE_ESTERILIZACAO) {
            throw new DomainException("Instrumento pendente de esterilização não pode ser utilizado");
        }
        if (this.status == StatusEsterilizacao.VENCIDO) {
            throw new DomainException("Instrumento com esterilização vencida não pode ser utilizado");
        }
    }

    public void marcarComoPendente() {
        this.status = StatusEsterilizacao.PENDENTE_ESTERILIZACAO;
    }

    public void atualizarEsterilizacao(LocalDate novaValidade) {
        this.status = StatusEsterilizacao.ESTERILIZADO;
        this.validadeEsterilizacao = novaValidade;
    }

    public void verificarValidade(LocalDate dataAtual) {
        if (this.status == StatusEsterilizacao.ESTERILIZADO && this.validadeEsterilizacao != null) {
            if (dataAtual.isAfter(this.validadeEsterilizacao)) {
                this.status = StatusEsterilizacao.VENCIDO;
            }
        }
    }
    public void setStatusParaTeste(StatusEsterilizacao status, LocalDate validade) {
        this.status = status;
        this.validadeEsterilizacao = validade;
    }
}