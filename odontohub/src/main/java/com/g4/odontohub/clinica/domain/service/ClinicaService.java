package com.g4.odontohub.clinica.domain.service;

import com.g4.odontohub.clinica.domain.model.Clinica;
import com.g4.odontohub.clinica.domain.model.ClinicaId;
import com.g4.odontohub.clinica.domain.model.StatusClinica;
import com.g4.odontohub.clinica.domain.repository.ClinicaRepository;

/** Serviço de domínio do contexto de Clínica (F18). */
public class ClinicaService {

    private static final int TAMANHO_MINIMO_SENHA = 6;

    private final ClinicaRepository repositorio;
    private final CodificadorSenha codificador;
    private Clinica ultimaClinica;

    public ClinicaService(ClinicaRepository repositorio, CodificadorSenha codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    /** Cadastra uma nova clínica, validando dados e unicidade de e-mail/CNPJ. */
    public Clinica cadastrar(String nome, String cnpj, String email, String senha) {
        validarNome(nome);
        validarCnpj(cnpj);
        validarEmail(email);
        validarSenha(senha);

        if (cnpjJaCadastrado(cnpj)) {
            throw new IllegalArgumentException("CNPJ já cadastrado.");
        }
        if (emailJaCadastrado(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        ClinicaId id = new ClinicaId(repositorio.proximoId());
        Clinica clinica = new Clinica(id, nome.trim(), cnpj.trim(), email.trim().toLowerCase(),
                codificador.codificar(senha), StatusClinica.ATIVA);
        repositorio.salvar(clinica);
        ultimaClinica = clinica;
        return clinica;
    }

    /**
     * Autentica a clínica por e-mail e senha. Lança exceção genérica quando as
     * credenciais não conferem (não revela se o erro foi no e-mail ou na senha).
     */
    public Clinica autenticar(String email, String senha) {
        Clinica clinica = email == null ? null
                : repositorio.buscarPorEmail(email.trim().toLowerCase());
        if (clinica == null || senha == null
                || !codificador.corresponde(senha, clinica.getSenhaHash())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        }
        if (!clinica.estaAtiva()) {
            throw new IllegalArgumentException("Clínica inativa.");
        }
        ultimaClinica = clinica;
        return clinica;
    }

    public boolean cnpjJaCadastrado(String cnpj) {
        return repositorio.buscarPorCnpj(cnpj.trim()) != null;
    }

    public boolean emailJaCadastrado(String email) {
        return repositorio.buscarPorEmail(email.trim().toLowerCase()) != null;
    }

    public Clinica buscarPorEmail(String email) {
        Clinica clinica = repositorio.buscarPorEmail(email == null ? null : email.trim().toLowerCase());
        if (clinica == null) {
            throw new IllegalArgumentException("Clínica não encontrada: " + email);
        }
        return clinica;
    }

    public java.util.List<Clinica> todas() {
        return repositorio.todas();
    }

    public Clinica getUltimaClinica() {
        return ultimaClinica;
    }

    private void validarNome(String nome) {
        if (vazio(nome)) {
            throw new IllegalArgumentException("Nome da clínica é obrigatório.");
        }
    }

    private void validarCnpj(String cnpj) {
        if (vazio(cnpj)) {
            throw new IllegalArgumentException("CNPJ é obrigatório.");
        }
    }

    private void validarEmail(String email) {
        if (vazio(email)) {
            throw new IllegalArgumentException("E-mail é obrigatório.");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
    }

    private void validarSenha(String senha) {
        if (vazio(senha)) {
            throw new IllegalArgumentException("Senha é obrigatória.");
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException("Senha deve ter no mínimo " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
