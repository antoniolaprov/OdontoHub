package com.g4.odontohub.equipe.domain.event;

import com.g4.odontohub.equipe.domain.model.FuncaoColaborador;

public class ColaboradorCadastrado {

    private final String nomeColaborador;
    private final FuncaoColaborador funcao;

    public ColaboradorCadastrado(String nomeColaborador, FuncaoColaborador funcao) {
        this.nomeColaborador = nomeColaborador;
        this.funcao = funcao;
    }

    public String getNomeColaborador() {
        return nomeColaborador;
    }

    public FuncaoColaborador getFuncao() {
        return funcao;
    }
}