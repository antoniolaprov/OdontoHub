package com.g4.odontohub.fichaClinica.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

@Getter
public class FichaClinica {

    private Long id;
    private String evolucao;
    private boolean confirmada;

    private FichaClinica() {}

    public static FichaClinica criar(Long id, String evolucao) {
        FichaClinica f = new FichaClinica();
        f.id = id;
        f.evolucao = evolucao;
        f.confirmada = false;
        return f;
    }

    public void confirmar() {
        if (this.confirmada) {
            throw new DomainException("Ficha clínica já está confirmada");
        }
        this.confirmada = true;
    }

    public void editarEvolucao(String novaEvolucao) {
        if (this.confirmada) {
            throw new DomainException("Evoluções confirmadas não podem ser editadas");
        }

        if (novaEvolucao == null || novaEvolucao.isBlank()) {
            throw new DomainException("Evolução não pode ser vazia");
        }

        this.evolucao = novaEvolucao;
    }
}