package com.g4.odontohub.inadimplencia.domain.repository;

import com.g4.odontohub.inadimplencia.domain.model.Paciente;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository {
    void salvar(Paciente paciente);
    Optional<Paciente> buscarPorId(String id);
    Optional<Paciente> buscarPorNome(String nome);
    List<Paciente> listarTodos();
}
