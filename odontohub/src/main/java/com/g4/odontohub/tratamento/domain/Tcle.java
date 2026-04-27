package com.g4.odontohub.tratamento.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Entidade de domínio puro: Termo de Consentimento Livre e Esclarecido (TCLE).
 * Segue DDD estrito: private constructor, static factory criar(), apenas @Getter.
 */
@Getter
public class Tcle {

    private Long id;
    private Long planoId;
    private String nomePaciente;
    private LocalDate dataAssinatura;
    private StatusTcle status;
    private String justificativaSubstituicao;

    private Tcle() {}

    /**
     * Cria um novo TCLE vinculado a um plano de tratamento com status PENDENTE.
     */
    public static Tcle criar(Long id, Long planoId, String nomePaciente) {
        if (planoId == null) {
            throw new DomainException("O TCLE deve estar vinculado a um plano de tratamento");
        }
        Tcle tcle = new Tcle();
        tcle.id = id;
        tcle.planoId = planoId;
        tcle.nomePaciente = nomePaciente;
        tcle.status = StatusTcle.PENDENTE;
        return tcle;
    }

    /**
     * Registra a assinatura do paciente, mudando o status para ASSINADO.
     */
    public void assinar(LocalDate dataAssinatura) {
        this.dataAssinatura = dataAssinatura;
        this.status = StatusTcle.ASSINADO;
    }

    /**
     * Marca o TCLE como SUBSTITUIDO e registra a justificativa.
     */
    public void substituir(String justificativa) {
        this.justificativaSubstituicao = justificativa;
        this.status = StatusTcle.SUBSTITUIDO;
    }

    /**
     * Valida que TCLEs assinados não podem ser excluídos.
     */
    public void validarExclusao() {
        if (this.status == StatusTcle.ASSINADO) {
            throw new DomainException("TCLEs assinados não podem ser excluídos");
        }
    }

    public boolean estaAssinado() {
        return this.status == StatusTcle.ASSINADO;
    }
}
