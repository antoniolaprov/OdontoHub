package com.g4.odontohub.equipe.domain.service;

import com.g4.odontohub.equipe.domain.event.ColaboradorCadastrado;
import com.g4.odontohub.equipe.domain.event.ColaboradorDesativado;
import com.g4.odontohub.equipe.domain.event.ColaboradorReativado;
import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.model.ColaboradorId;
import com.g4.odontohub.equipe.domain.model.FuncaoColaborador;
import com.g4.odontohub.equipe.domain.repository.ColaboradorRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.List;
import java.util.stream.Collectors;

public class ColaboradorService {

    private final ColaboradorRepository repositorio;

    public ColaboradorService(ColaboradorRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Colaborador cadastrar(String nomeCompleto, String cpf, String telefone, String funcaoLabel) {
        if (funcaoLabel == null || funcaoLabel.isBlank()) {
            throw new IllegalArgumentException("A função do colaborador é obrigatória");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório para o cadastro de colaboradores");
        }
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new IllegalArgumentException("Nome completo é obrigatório");
        }
        FuncaoColaborador funcao = FuncaoColaborador.fromLabel(funcaoLabel);
        ColaboradorId id = new ColaboradorId(repositorio.proximoId());
        Colaborador colaborador = new Colaborador(id, nomeCompleto, cpf, telefone, funcao);
        repositorio.salvar(colaborador);
        DomainEventPublisher.publish(new ColaboradorCadastrado(id, funcao));
        return colaborador;
    }

    public void desativar(String nome) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.desativar();
        repositorio.salvar(colaborador);
        DomainEventPublisher.publish(new ColaboradorDesativado(colaborador.getId()));
    }

    public void reativar(String nome) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.reativar();
        repositorio.salvar(colaborador);
        DomainEventPublisher.publish(new ColaboradorReativado(colaborador.getId()));
    }

    public List<Colaborador> listarResponsaveisEsterilizacao() {
        return repositorio.todos().stream()
                .filter(c -> c.getFuncao() == FuncaoColaborador.AUXILIAR)
                .filter(Colaborador::estaAtivo)
                .collect(Collectors.toList());
    }

    public Colaborador buscarPorNome(String nome) {
        Colaborador colaborador = repositorio.buscarPorNome(nome);
        if (colaborador == null) {
            throw new IllegalArgumentException("Colaborador não encontrado: " + nome);
        }
        return colaborador;
    }

    public boolean existe(String nome) {
        return repositorio.buscarPorNome(nome) != null;
    }
}
