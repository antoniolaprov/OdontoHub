package com.g4.odontohub.equipe.domain.service;

import com.g4.odontohub.equipe.domain.event.ColaboradorCadastrado;
import com.g4.odontohub.equipe.domain.event.ColaboradorDesativado;
import com.g4.odontohub.equipe.domain.event.ColaboradorReativado;
import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.model.Cpf;
import com.g4.odontohub.equipe.domain.model.FuncaoColaborador;
import com.g4.odontohub.equipe.domain.model.StatusColaborador;

import java.util.List;

public class EquipeDomainService {

    public ColaboradorCadastrado cadastrar(String nome, String cpfValor, String telefone, String funcaoStr) {
        if (funcaoStr == null || funcaoStr.isBlank()) {
            throw new IllegalArgumentException("A função do colaborador é obrigatória");
        }
        Cpf cpf = new Cpf(cpfValor);
        FuncaoColaborador funcao = FuncaoColaborador.valueOf(funcaoStr.toUpperCase());
        Colaborador colaborador = new Colaborador(nome, cpf, telefone, funcao);
        return new ColaboradorCadastrado(colaborador.getNome(), colaborador.getFuncao());
    }

    public ColaboradorDesativado desativar(Colaborador colaborador) {
        colaborador.desativar();
        return new ColaboradorDesativado(colaborador.getNome());
    }

    public ColaboradorReativado reativar(Colaborador colaborador) {
        colaborador.reativar();
        return new ColaboradorReativado(colaborador.getNome());
    }

    public List<Colaborador> listarResponsaveisEsterilizacao(List<Colaborador> todos) {
        return todos.stream()
                .filter(c -> c.getFuncao() == FuncaoColaborador.AUXILIAR)
                .filter(Colaborador::isAtivo)
                .toList();
    }
}