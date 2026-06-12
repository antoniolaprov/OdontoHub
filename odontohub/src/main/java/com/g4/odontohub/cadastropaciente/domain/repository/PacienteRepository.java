package com.g4.odontohub.cadastropaciente.domain.repository;

import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;

import java.util.List;

/** Porta de persistência do contexto de Cadastro de Paciente (camada de domínio). */
public interface PacienteRepository {

    void salvar(Paciente paciente);

    Paciente buscarPorId(PacienteRegistroId id);

    Paciente buscarPorNome(String nomeCompleto);

    List<Paciente> todos();

    long proximoId();
}
