package com.g4.odontohub.prontuario.domain;

import java.util.Optional;

public interface AnamneseRepository {
    Anamnese salvar(Anamnese anamnese);
    Optional<Anamnese> buscarPorPacienteId(Long pacienteId);
}
