package com.g4.odontohub.cadastropaciente.domain.model;

import java.time.LocalDate;
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
        this.status = StatusPaciente.RESTRITO;
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
            case "nomeCompleto" -> this.nomeCompleto = novoValor;
            case "cpf" -> this.cpf = novoValor;
            case "dataNascimento" -> this.dataNascimento = novoValor;
            case "telefone" -> this.telefone = novoValor;
            case "email" -> this.email = novoValor;
            default -> throw new IllegalArgumentException("Campo desconhecido: " + campo);
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
