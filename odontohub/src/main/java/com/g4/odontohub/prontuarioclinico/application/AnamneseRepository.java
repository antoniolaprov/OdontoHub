package com.g4.odontohub.prontuarioclinico.application;

import com.g4.odontohub.prontuarioclinico.domain.model.Anamnese;

public interface AnamneseRepository {
    void salvar(Anamnese anamnese);
    Anamnese buscarPorPacienteId(Long pacienteId);
}