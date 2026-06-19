package com.g4.odontohub.cadastropaciente.domain.service;

import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;
import com.g4.odontohub.cadastropaciente.domain.model.StatusPaciente;
import com.g4.odontohub.cadastropaciente.domain.repository.PacienteRepository;

import java.util.List;

public class PacienteService {

    private final PacienteRepository repositorio;
    private Paciente ultimoPaciente;

    public PacienteService(PacienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void cadastrarCompleto(String nomeCompleto, String cpf, String dataNascimento,
                                  String telefone, String email, String responsavel) {
        validarNome(nomeCompleto);
        validarTelefone(telefone);
        if (vazio(cpf)) {
            throw new IllegalArgumentException("CPF é obrigatório no cadastro completo.");
        }
        if (cpfJaCadastrado(cpf)) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }
        if (!vazio(email) && emailJaCadastrado(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        PacienteRegistroId id = new PacienteRegistroId(repositorio.proximoId());
        ultimoPaciente = new Paciente(id, nomeCompleto, cpf, dataNascimento, telefone, email, StatusPaciente.ATIVO);
        repositorio.salvar(ultimoPaciente);
    }

    public void cadastrarRapido(String nomeCompleto, String telefone, String responsavel) {
        validarNome(nomeCompleto);
        validarTelefone(telefone);

        PacienteRegistroId id = new PacienteRegistroId(repositorio.proximoId());
        ultimoPaciente = new Paciente(id, nomeCompleto, null, null, telefone, null, StatusPaciente.INCOMPLETO);
        repositorio.salvar(ultimoPaciente);
    }

    public void atualizarCadastro(PacienteRegistroId pacienteId, String campo, String novoValor, String responsavel) {
        Paciente paciente = buscarPorId(pacienteId);
        paciente.atualizarCadastro(campo, novoValor, responsavel);
        repositorio.salvar(paciente);
        ultimoPaciente = paciente;
    }

    public void restringirPaciente(PacienteRegistroId pacienteId) {
        Paciente paciente = buscarPorId(pacienteId);
        paciente.restringir();
        repositorio.salvar(paciente);
        ultimoPaciente = paciente;
    }

    public boolean cpfJaCadastrado(String cpf) {
        return repositorio.todos().stream()
                .anyMatch(p -> p.getCpf() != null && p.getCpf().equals(cpf));
    }

    public boolean emailJaCadastrado(String email) {
        return repositorio.todos().stream()
                .anyMatch(p -> p.getEmail() != null && p.getEmail().equals(email));
    }

    public Paciente buscarPorId(PacienteRegistroId pacienteId) {
        Paciente paciente = repositorio.buscarPorId(pacienteId);
        if (paciente == null) {
            throw new IllegalArgumentException("Paciente não encontrado: " + pacienteId.id());
        }
        return paciente;
    }

    public Paciente buscarPorNome(String nomeCompleto) {
        Paciente paciente = repositorio.buscarPorNome(nomeCompleto);
        if (paciente == null) {
            throw new IllegalArgumentException("Paciente não encontrado: " + nomeCompleto);
        }
        return paciente;
    }

    public Paciente getUltimoPaciente() {
        return ultimoPaciente;
    }

    public List<Paciente> listarTodos() {
        return repositorio.todos();
    }

    private void validarNome(String nomeCompleto) {
        if (vazio(nomeCompleto)) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
    }

    private void validarTelefone(String telefone) {
        if (vazio(telefone)) {
            throw new IllegalArgumentException("Telefone é obrigatório.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
