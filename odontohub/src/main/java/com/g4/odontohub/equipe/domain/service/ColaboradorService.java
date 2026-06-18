package com.g4.odontohub.equipe.domain.service;

import com.g4.odontohub.equipe.domain.event.ColaboradorCadastrado;
import com.g4.odontohub.equipe.domain.event.ColaboradorDesativado;
import com.g4.odontohub.equipe.domain.event.ColaboradorReativado;
import com.g4.odontohub.equipe.domain.model.*;
import com.g4.odontohub.equipe.domain.repository.ColaboradorRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ColaboradorService {

    private final ColaboradorRepository repositorio;

    public ColaboradorService(ColaboradorRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Colaborador cadastrar(String nomeCompleto, String cpf, String telefone, String funcaoLabel) {
        return cadastrarCompleto(nomeCompleto, cpf, telefone, null, null, null, funcaoLabel);
    }

    public Colaborador cadastrarCompleto(String nomeCompleto, String cpf, String telefone,
                                         String email, String login, String senha, String funcaoLabel) {
        if (funcaoLabel == null || funcaoLabel.isBlank()) {
            throw new IllegalArgumentException("A função do colaborador é obrigatória");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório para o cadastro de colaboradores");
        }
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new IllegalArgumentException("Nome completo é obrigatório");
        }
        validarUnicidade(cpf, email, login);
        FuncaoColaborador funcao = FuncaoColaborador.fromLabel(funcaoLabel);
        ColaboradorId id = new ColaboradorId(repositorio.proximoId());
        Colaborador colaborador = new Colaborador(id, nomeCompleto, cpf, telefone, email, login, senha, funcao);
        repositorio.salvar(colaborador);
        DomainEventPublisher.publish(new ColaboradorCadastrado(id, funcao));
        return colaborador;
    }

    private void validarUnicidade(String cpf, String email, String login) {
        for (Colaborador existente : repositorio.todos()) {
            if (cpf != null && cpf.equalsIgnoreCase(existente.getCpf())) {
                throw new IllegalArgumentException("Já existe um colaborador com este CPF");
            }
            if (email != null && !email.isBlank() && email.equalsIgnoreCase(existente.getEmail())) {
                throw new IllegalArgumentException("Já existe um colaborador com este e-mail");
            }
            if (login != null && !login.isBlank() && login.equalsIgnoreCase(existente.getLogin())) {
                throw new IllegalArgumentException("Já existe um colaborador com este login");
            }
        }
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

    public void alterarStatus(String nome, String statusLabel) {
        StatusColaborador novoStatus = StatusColaborador.fromLabel(statusLabel);
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.alterarStatus(novoStatus);
        repositorio.salvar(colaborador);
    }

    /** Tenta autenticar o colaborador; bloqueia a conta após tentativas inválidas. */
    public boolean tentarLogin(String nome, String senha) {
        Colaborador colaborador = buscarPorNome(nome);
        boolean sucesso = colaborador.tentarAutenticar(senha);
        repositorio.salvar(colaborador);
        return sucesso;
    }

    public boolean estaBloqueado(String nome) {
        return buscarPorNome(nome).estaBloqueado();
    }

    public List<Colaborador> listarResponsaveisEsterilizacao() {
        return repositorio.todos().stream()
                .filter(c -> c.getFuncao() == FuncaoColaborador.AUXILIAR)
                .filter(Colaborador::estaAtivo)
                .collect(Collectors.toList());
    }

    // ----- Disponibilidade (F12) -----

    public void definirDisponibilidade(String nome, Set<DiaSemana> dias, int horaInicio, int horaFim) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.definirDisponibilidade(new Disponibilidade(dias, horaInicio, horaFim));
        repositorio.salvar(colaborador);
    }

    public void registrarAusencia(String nome, LocalDate inicio, LocalDate fim, String tipoLabel, String motivo) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.registrarAusencia(new PeriodoAusencia(inicio, fim, TipoAusencia.fromLabel(tipoLabel), motivo));
        repositorio.salvar(colaborador);
    }

    public boolean estaDisponivelEm(String nome, LocalDateTime momento) {
        return buscarPorNome(nome).estaDisponivelEm(momento);
    }

    // ----- Auditoria e rastreabilidade (F12) -----

    public void registrarAcao(String nome, String moduloLabel, String acao) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.registrarAcao(ModuloSistema.fromLabel(moduloLabel), acao);
        repositorio.salvar(colaborador);
    }

    public List<RegistroAuditoria> auditoriaDe(String nome) {
        return buscarPorNome(nome).getAuditoria();
    }

    public List<RegistroAuditoria> rastrearAcoes(String nome, String moduloLabel) {
        return buscarPorNome(nome).acoesDoModulo(ModuloSistema.fromLabel(moduloLabel));
    }

    // ----- Histórico de alterações cadastrais (F12) -----

    public void alterarDado(String nome, String campo, String novoValor, String responsavel) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.alterarDado(campo, novoValor, responsavel);
        repositorio.salvar(colaborador);
    }

    public List<AlteracaoCadastral> historicoAlteracoesDe(String nome) {
        return buscarPorNome(nome).getHistoricoAlteracoes();
    }

    // ----- Indicadores de desempenho (F12) -----

    public void registrarAtendimento(String nome) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.registrarAtendimento();
        repositorio.salvar(colaborador);
    }

    public void registrarFalta(String nome) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.registrarFalta();
        repositorio.salvar(colaborador);
    }

    public void registrarConversao(String nome) {
        Colaborador colaborador = buscarPorNome(nome);
        colaborador.registrarConversao();
        repositorio.salvar(colaborador);
    }

    public IndicadoresDesempenho indicadoresDe(String nome) {
        return buscarPorNome(nome).indicadores();
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
