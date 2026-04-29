package com.g4.odontohub.equipe.application;

import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.model.Cpf;
import com.g4.odontohub.equipe.domain.model.FuncaoColaborador;
import com.g4.odontohub.equipe.domain.service.EquipeDomainService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipeApplicationService {

    private final EquipeDomainService domainService = new EquipeDomainService();
    private final Map<String, Colaborador> colaboradores = new HashMap<>();
    private String ultimoErro;

    public void cadastrarColaborador(String nome, String cpf, String telefone, String funcao) {
        try {
            domainService.cadastrar(nome, cpf, telefone, funcao);
            Colaborador colaborador = new Colaborador(
                nome,
                new Cpf(cpf),
                telefone,
                FuncaoColaborador.valueOf(funcao.toUpperCase())
            );
            colaboradores.put(nome, colaborador);
            ultimoErro = null;
        } catch (IllegalArgumentException e) {
            ultimoErro = e.getMessage();
        }
    }

    public void cadastrarSemFuncao(String nome, String cpf, String telefone) {
        try {
            domainService.cadastrar(nome, cpf, telefone, null);
            ultimoErro = null;
        } catch (IllegalArgumentException e) {
            ultimoErro = e.getMessage();
        }
    }

    public void cadastrarSemCpf(String nome) {
        try {
            domainService.cadastrar(nome, null, null, "AUXILIAR");
            ultimoErro = null;
        } catch (IllegalArgumentException e) {
            ultimoErro = e.getMessage();
        }
    }

    public void desativarColaborador(String nome) {
        Colaborador colaborador = colaboradores.get(nome);
        if (colaborador != null) {
            domainService.desativar(colaborador);
        }
    }

    public void reativarColaborador(String nome) {
        Colaborador colaborador = colaboradores.get(nome);
        if (colaborador != null) {
            domainService.reativar(colaborador);
        }
    }

    public void adicionarColaboradorComStatus(String nome, String funcao, String status) {
        Colaborador colaborador = new Colaborador(
            nome,
            new Cpf("000.000.000-00"),
            "00000000000",
            FuncaoColaborador.valueOf(funcao.toUpperCase())
        );
        if (status.equalsIgnoreCase("Inativo")) {
            colaborador.desativar();
        }
        colaboradores.put(nome, colaborador);
    }

    public List<Colaborador> listarResponsaveisEsterilizacao() {
        return domainService.listarResponsaveisEsterilizacao(new ArrayList<>(colaboradores.values()));
    }

    public Colaborador getColaborador(String nome) {
        return colaboradores.get(nome);
    }

    public boolean colaboradorExisteNoSistema(String nome) {
        return colaboradores.containsKey(nome);
    }

    public String getUltimoErro() {
        return ultimoErro;
    }

    public boolean houveErro() {
        return ultimoErro != null;
    }
}