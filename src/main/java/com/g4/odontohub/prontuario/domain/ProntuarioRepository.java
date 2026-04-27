package com.g4.odontohub.prontuario.domain;

import java.util.Optional;

public interface ProntuarioRepository {
    Prontuario salvar(Prontuario prontuario);
    Optional<Prontuario> buscarPorId(Long id);
    Optional<Prontuario> buscarPorPacienteId(Long pacienteId);
}
