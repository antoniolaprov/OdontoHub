package com.g4.odontohub.equipe.domain.model;

public class Colaborador {

    /** Tentativas de login inválidas a partir das quais a conta é bloqueada. */
    private static final int LIMITE_TENTATIVAS_LOGIN = 3;

    private final ColaboradorId id;
    private final String nomeCompleto;
    private final String cpf;
    private final String telefone;
    private final String email;
    private final String login;
    private final String senha;
    private final FuncaoColaborador funcao;
    private StatusColaborador status;
    private int tentativasLoginInvalidas;
    private boolean bloqueado;

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
                                           StatusColaborador status, int tentativasLoginInvalidas, boolean bloqueado) {
        Colaborador c = new Colaborador(id, nomeCompleto, cpf, telefone, email, login, senha, funcao);
        c.status = status;
        c.tentativasLoginInvalidas = tentativasLoginInvalidas;
        c.bloqueado = bloqueado;
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
}
