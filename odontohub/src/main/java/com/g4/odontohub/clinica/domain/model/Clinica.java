package com.g4.odontohub.clinica.domain.model;

/**
 * Agregado raiz do contexto de Clínica (F18).
 * Representa a conta da clínica que acessa o OdontoHub. A senha nunca é guardada
 * em texto puro — apenas o hash gerado pela porta {@code CodificadorSenha}.
 */
public class Clinica {

    private final ClinicaId id;
    private String nome;
    private String cnpj;
    private String email;
    private String senhaHash;
    private StatusClinica status;

    public Clinica(ClinicaId id, String nome, String cnpj, String email,
                   String senhaHash, StatusClinica status) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.email = email;
        this.senhaHash = senhaHash;
        this.status = status;
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Clinica reconstituir(ClinicaId id, String nome, String cnpj, String email,
                                       String senhaHash, StatusClinica status) {
        return new Clinica(id, nome, cnpj, email, senhaHash, status);
    }

    public void inativar() {
        this.status = StatusClinica.INATIVA;
    }

    public boolean estaAtiva() {
        return status == StatusClinica.ATIVA;
    }

    public ClinicaId getId() { return id; }
    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public StatusClinica getStatus() { return status; }
}
