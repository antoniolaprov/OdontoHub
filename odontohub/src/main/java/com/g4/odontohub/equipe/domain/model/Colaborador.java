package com.g4.odontohub.equipe.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Colaborador {

    /** Tentativas de login inválidas a partir das quais a conta é bloqueada. */
    private static final int LIMITE_TENTATIVAS_LOGIN = 3;

    private final ColaboradorId id;
    private final String nomeCompleto;
    private final String cpf;
    private String telefone;
    private String email;
    private final String login;
    private final String senha;
    private final FuncaoColaborador funcao;
    private StatusColaborador status;
    private int tentativasLoginInvalidas;
    private boolean bloqueado;
    private Disponibilidade disponibilidade;
    private final List<RegistroAuditoria> auditoria = new ArrayList<>();
    private final List<AlteracaoCadastral> historicoAlteracoes = new ArrayList<>();
    private int atendimentos;
    private int faltas;
    private int conversoes;

    public Colaborador(ColaboradorId id, String nomeCompleto, String cpf, String telefone, FuncaoColaborador funcao) {
        this(id, nomeCompleto, cpf, telefone, null, null, null, funcao);
    }

    public Colaborador(ColaboradorId id, String nomeCompleto, String cpf, String telefone,
                       String email, String login, String senha, FuncaoColaborador funcao) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.login = login;
        this.senha = senha;
        this.funcao = funcao;
        this.status = StatusColaborador.ATIVO;
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Colaborador reconstituir(ColaboradorId id, String nomeCompleto, String cpf, String telefone,
                                           String email, String login, String senha, FuncaoColaborador funcao,
                                           StatusColaborador status, int tentativasLoginInvalidas, boolean bloqueado,
                                           Disponibilidade disponibilidade, List<RegistroAuditoria> auditoria,
                                           List<AlteracaoCadastral> historicoAlteracoes,
                                           int atendimentos, int faltas, int conversoes) {
        Colaborador c = new Colaborador(id, nomeCompleto, cpf, telefone, email, login, senha, funcao);
        c.status = status;
        c.tentativasLoginInvalidas = tentativasLoginInvalidas;
        c.bloqueado = bloqueado;
        c.disponibilidade = disponibilidade;
        if (auditoria != null) {
            c.auditoria.addAll(auditoria);
        }
        if (historicoAlteracoes != null) {
            c.historicoAlteracoes.addAll(historicoAlteracoes);
        }
        c.atendimentos = atendimentos;
        c.faltas = faltas;
        c.conversoes = conversoes;
        return c;
    }

    public void desativar() {
        this.status = StatusColaborador.INATIVO;
    }

    public void reativar() {
        this.status = StatusColaborador.ATIVO;
    }

    public void alterarStatus(StatusColaborador novoStatus) {
        this.status = novoStatus;
    }

    public boolean estaAtivo() {
        return status == StatusColaborador.ATIVO;
    }

    // ----- Permissões (derivadas automaticamente da função) -----

    public boolean podeValidarProcedimentos() {
        return funcao.podeValidarProcedimentos();
    }

    public boolean podeAlterarPermissoes() {
        return funcao.podeAlterarPermissoes();
    }

    public boolean podeAcessarFinanceiro() {
        return funcao.podeAcessarFinanceiro();
    }

    // ----- Segurança de login -----

    public boolean podeFazerLogin() {
        return status.permiteLogin() && !bloqueado;
    }

    /**
     * Tenta autenticar com a senha informada. Retorna true em caso de sucesso.
     * Após {@value #LIMITE_TENTATIVAS_LOGIN} tentativas inválidas, a conta é bloqueada.
     */
    public boolean tentarAutenticar(String senhaInformada) {
        if (!podeFazerLogin()) {
            return false;
        }
        if (senha != null && senha.equals(senhaInformada)) {
            tentativasLoginInvalidas = 0;
            return true;
        }
        tentativasLoginInvalidas++;
        if (tentativasLoginInvalidas >= LIMITE_TENTATIVAS_LOGIN) {
            bloqueado = true;
        }
        return false;
    }

    public boolean estaBloqueado() {
        return bloqueado;
    }

    // ----- Disponibilidade (F12) -----

    public void definirDisponibilidade(Disponibilidade novaDisponibilidade) {
        this.disponibilidade = novaDisponibilidade;
    }

    public void registrarAusencia(PeriodoAusencia ausencia) {
        if (disponibilidade == null) {
            throw new IllegalStateException("Defina a disponibilidade antes de registrar ausências");
        }
        disponibilidade.registrarAusencia(ausencia);
    }

    /** Verifica se o colaborador está disponível para agendamento no momento informado. */
    public boolean estaDisponivelEm(LocalDateTime momento) {
        return disponibilidade == null || disponibilidade.estaDisponivelEm(momento);
    }

    // ----- Auditoria e rastreabilidade (F12) -----

    public void registrarAcao(ModuloSistema modulo, String acao) {
        auditoria.add(new RegistroAuditoria(LocalDateTime.now(), modulo, acao));
    }

    /** Rastreabilidade operacional: ações registradas para um módulo específico. */
    public List<RegistroAuditoria> acoesDoModulo(ModuloSistema modulo) {
        return auditoria.stream().filter(r -> r.modulo() == modulo).toList();
    }

    // ----- Histórico de alterações cadastrais (F12) -----

    /** Altera um dado cadastral editável (telefone, e-mail) preservando o histórico. */
    public void alterarDado(String campo, String novoValor, String responsavel) {
        String chave = campo == null ? "" : campo.trim().toLowerCase();
        String valorAnterior;
        switch (chave) {
            case "telefone" -> {
                valorAnterior = this.telefone;
                this.telefone = novoValor;
            }
            case "email", "e-mail" -> {
                valorAnterior = this.email;
                this.email = novoValor;
            }
            default -> throw new IllegalArgumentException("Campo cadastral não editável: " + campo);
        }
        historicoAlteracoes.add(new AlteracaoCadastral(chave, valorAnterior, novoValor, responsavel, LocalDateTime.now()));
    }

    // ----- Indicadores de desempenho (F12) -----

    public void registrarAtendimento() { this.atendimentos++; }
    public void registrarFalta() { this.faltas++; }
    public void registrarConversao() { this.conversoes++; }

    public IndicadoresDesempenho indicadores() {
        return new IndicadoresDesempenho(atendimentos, faltas, conversoes);
    }

    public ColaboradorId getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getLogin() { return login; }
    /** Acesso à senha restrito à camada de persistência (reconstituição do agregado). */
    public String getSenhaPersistencia() { return senha; }
    public FuncaoColaborador getFuncao() { return funcao; }
    public StatusColaborador getStatus() { return status; }
    public int getTentativasLoginInvalidas() { return tentativasLoginInvalidas; }
    public Disponibilidade getDisponibilidade() { return disponibilidade; }
    public List<RegistroAuditoria> getAuditoria() { return Collections.unmodifiableList(auditoria); }
    public List<AlteracaoCadastral> getHistoricoAlteracoes() { return Collections.unmodifiableList(historicoAlteracoes); }
    public int getAtendimentos() { return atendimentos; }
    public int getFaltas() { return faltas; }
    public int getConversoes() { return conversoes; }
}
