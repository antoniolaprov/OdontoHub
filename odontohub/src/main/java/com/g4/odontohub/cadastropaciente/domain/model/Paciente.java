package com.g4.odontohub.cadastropaciente.domain.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Paciente {

    private final PacienteRegistroId id;
    private String nomeCompleto;
    private String cpf;
    private String dataNascimento;
    private String telefone;
    private String email;
    private StatusPaciente status;
    private final List<AlteracaoCadastral> historicoAlteracoes = new ArrayList<>();

    public Paciente(PacienteRegistroId id, String nomeCompleto, String cpf, String dataNascimento,
                    String telefone, String email, StatusPaciente status) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.email = email;
        this.status = status;
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Paciente reconstituir(PacienteRegistroId id, String nomeCompleto, String cpf,
                                        String dataNascimento, String telefone, String email,
                                        StatusPaciente status, List<AlteracaoCadastral> historico) {
        Paciente p = new Paciente(id, nomeCompleto, cpf, dataNascimento, telefone, email, status);
        if (historico != null) {
            p.historicoAlteracoes.addAll(historico);
        }
        return p;
    }

    public void atualizarCadastro(String campo, String novoValor, String responsavel) {
        String valorAnterior = valorDoCampo(campo);
        alterarCampo(campo, novoValor);
        historicoAlteracoes.add(new AlteracaoCadastral(
                campo, valorAnterior, novoValor, responsavel, LocalDate.now()));
    }

    public void restringir() {
        atualizarStatus(StatusPaciente.RESTRITO);
    }

    /** Transição genérica de status (ex.: reverter um paciente Restrito para Ativo). */
    public void atualizarStatus(StatusPaciente novoStatus) {
        this.status = novoStatus;
    }

    private String valorDoCampo(String campo) {
        return switch (campo) {
            case "nomeCompleto" -> nomeCompleto;
            case "cpf" -> cpf;
            case "dataNascimento" -> dataNascimento;
            case "telefone" -> telefone;
            case "email" -> email;
            default -> throw new IllegalArgumentException("Campo desconhecido: " + campo);
        };
    }

    private void alterarCampo(String campo, String novoValor) {
        switch (campo) {
            case "nomeCompleto" -> {
                validarNomeFormato(novoValor);
                this.nomeCompleto = novoValor;
            }
            case "cpf" -> this.cpf = novoValor;
            case "dataNascimento" -> {
                validarDataNascimentoFormato(novoValor);
                this.dataNascimento = novoValor;
            }
            case "telefone" -> this.telefone = novoValor;
            case "email" -> {
                validarEmailFormato(novoValor);
                this.email = novoValor;
            }
            default -> throw new IllegalArgumentException("Campo desconhecido: " + campo);
        }
    }

    private static final DateTimeFormatter FORMATO_DATA_NASCIMENTO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Exige ao menos uma letra — rejeita nomes vazios ou só com números/símbolos. */
    public static void validarNomeFormato(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (!nome.matches(".*[A-Za-zÀ-ÖØ-öø-ÿ].*")) {
            throw new IllegalArgumentException("Nome deve conter ao menos uma letra.");
        }
    }

    /** E-mail é opcional, mas se informado precisa ter formato válido. */
    public static void validarEmailFormato(String email) {
        if (email != null && !email.isBlank() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("E-mail em formato inválido.");
        }
    }

    /** Data de nascimento, quando informada, precisa ser uma data de calendário válida (dd/MM/yyyy) e não futura. */
    public static void validarDataNascimentoFormato(String dataNascimento) {
        if (dataNascimento == null || dataNascimento.isBlank()) {
            return;
        }
        LocalDate data;
        try {
            data = LocalDate.parse(dataNascimento, FORMATO_DATA_NASCIMENTO);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de nascimento inválida.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro.");
        }
    }

    public PacienteRegistroId getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getCpf() { return cpf; }
    public String getDataNascimento() { return dataNascimento; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public StatusPaciente getStatus() { return status; }
    public List<AlteracaoCadastral> getHistoricoAlteracoes() {
        return Collections.unmodifiableList(historicoAlteracoes);
    }
}
