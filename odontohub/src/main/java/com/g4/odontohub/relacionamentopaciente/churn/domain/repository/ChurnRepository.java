package com.g4.odontohub.relacionamentopaciente.churn.domain.repository;

import com.g4.odontohub.relacionamentopaciente.churn.domain.model.AnaliseChurn;

import java.util.List;

/** Porta de persistência do agregado AnaliseChurn (camada de domínio). */
public interface ChurnRepository {

    void salvar(AnaliseChurn analise);

    AnaliseChurn buscarPorPaciente(String paciente);

    List<AnaliseChurn> todos();

    long proximoId();
}
