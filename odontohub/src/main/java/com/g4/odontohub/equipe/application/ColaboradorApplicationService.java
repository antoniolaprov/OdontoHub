package com.g4.odontohub.equipe.application;

import com.g4.odontohub.equipe.domain.model.*;
import com.g4.odontohub.equipe.domain.repository.ColaboradorRepository;
import com.g4.odontohub.equipe.domain.service.ColaboradorService;
import com.g4.odontohub.equipe.infrastructure.persistence.InMemoryColaboradorRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    public List<Colaborador> todos() {
        return service.todos();
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

    // ----- Disponibilidade -----

    public void definirDisponibilidade(String nome, Set<DiaSemana> dias, int horaInicio, int horaFim) {
        service.definirDisponibilidade(nome, dias, horaInicio, horaFim);
    }

    public void registrarAusencia(String nome, LocalDate inicio, LocalDate fim, String tipo, String motivo) {
        service.registrarAusencia(nome, inicio, fim, tipo, motivo);
    }

    public boolean estaDisponivelEm(String nome, LocalDateTime momento) {
        return service.estaDisponivelEm(nome, momento);
    }

    // ----- Auditoria e rastreabilidade -----

    public void registrarAcao(String nome, String modulo, String acao) {
        service.registrarAcao(nome, modulo, acao);
    }

    public List<RegistroAuditoria> auditoriaDe(String nome) {
        return service.auditoriaDe(nome);
    }

    public List<RegistroAuditoria> rastrearAcoes(String nome, String modulo) {
        return service.rastrearAcoes(nome, modulo);
    }

    // ----- Histórico de alterações -----

    public void alterarDado(String nome, String campo, String novoValor, String responsavel) {
        service.alterarDado(nome, campo, novoValor, responsavel);
    }

    public List<AlteracaoCadastral> historicoAlteracoesDe(String nome) {
        return service.historicoAlteracoesDe(nome);
    }

    // ----- Indicadores de desempenho -----

    public void registrarAtendimento(String nome) {
        service.registrarAtendimento(nome);
    }

    public void registrarFalta(String nome) {
        service.registrarFalta(nome);
    }

    public void registrarConversao(String nome) {
        service.registrarConversao(nome);
    }

    public IndicadoresDesempenho indicadoresDe(String nome) {
        return service.indicadoresDe(nome);
    }
}
