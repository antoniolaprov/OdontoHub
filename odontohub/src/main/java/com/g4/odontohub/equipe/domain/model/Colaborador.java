package com.g4.odontohub.equipe.domain.model;

public class Colaborador {

    private final ColaboradorId id;
    private final String nomeCompleto;
    private final String cpf;
    private final String telefone;
    private final FuncaoColaborador funcao;
    private StatusColaborador status;

    public Colaborador(ColaboradorId id, String nomeCompleto, String cpf, String telefone, FuncaoColaborador funcao) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.telefone = telefone;
        this.funcao = funcao;
        this.status = StatusColaborador.ATIVO;
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Colaborador reconstituir(ColaboradorId id, String nomeCompleto, String cpf,
                                           String telefone, FuncaoColaborador funcao, StatusColaborador status) {
        Colaborador c = new Colaborador(id, nomeCompleto, cpf, telefone, funcao);
        c.status = status;
        return c;
    }

    public void desativar() {
        this.status = StatusColaborador.INATIVO;
    }

    public void reativar() {
        this.status = StatusColaborador.ATIVO;
    }

    public boolean estaAtivo() {
        return status == StatusColaborador.ATIVO;
    }

    public ColaboradorId getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public FuncaoColaborador getFuncao() { return funcao; }
    public StatusColaborador getStatus() { return status; }
}
