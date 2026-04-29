package com.g4.odontohub.equipe.domain.model;

public class Colaborador {

    private final String nome;
    private final Cpf cpf;
    private final String telefone;
    private final FuncaoColaborador funcao;
    private StatusColaborador status;

    public Colaborador(String nome, Cpf cpf, String telefone, FuncaoColaborador funcao) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.funcao = funcao;
        this.status = StatusColaborador.ATIVO;
    }

    public String getNome() {
        return nome;
    }

    public Cpf getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public FuncaoColaborador getFuncao() {
        return funcao;
    }

    public StatusColaborador getStatus() {
        return status;
    }

    public void desativar() {
        this.status = StatusColaborador.INATIVO;
    }

    public void reativar() {
        this.status = StatusColaborador.ATIVO;
    }

    public boolean isAtivo() {
        return this.status == StatusColaborador.ATIVO;
    }
}