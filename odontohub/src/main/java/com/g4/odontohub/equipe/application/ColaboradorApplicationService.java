package com.g4.odontohub.equipe.application;

import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.repository.ColaboradorRepository;
import com.g4.odontohub.equipe.domain.service.ColaboradorService;
import com.g4.odontohub.equipe.infrastructure.persistence.InMemoryColaboradorRepository;

import java.util.List;

public class ColaboradorApplicationService {

    private final ColaboradorService service;

    public ColaboradorApplicationService() {
        this(new InMemoryColaboradorRepository());
    }

    public ColaboradorApplicationService(ColaboradorRepository repositorio) {
        this.service = new ColaboradorService(repositorio);
    }

    public Colaborador cadastrar(String nomeCompleto, String cpf, String telefone, String funcao) {
        return service.cadastrar(nomeCompleto, cpf, telefone, funcao);
    }

    public Colaborador cadastrarCompleto(String nomeCompleto, String cpf, String telefone,
                                         String email, String login, String senha, String funcao) {
        return service.cadastrarCompleto(nomeCompleto, cpf, telefone, email, login, senha, funcao);
    }

    public void desativar(String nome) {
        service.desativar(nome);
    }

    public void reativar(String nome) {
        service.reativar(nome);
    }

    public void alterarStatus(String nome, String status) {
        service.alterarStatus(nome, status);
    }

    public boolean tentarLogin(String nome, String senha) {
        return service.tentarLogin(nome, senha);
    }

    public boolean estaBloqueado(String nome) {
        return service.estaBloqueado(nome);
    }

    public List<Colaborador> listarResponsaveisEsterilizacao() {
        return service.listarResponsaveisEsterilizacao();
    }

    public Colaborador buscarPorNome(String nome) {
        return service.buscarPorNome(nome);
    }

    public boolean existe(String nome) {
        return service.existe(nome);
    }
}
