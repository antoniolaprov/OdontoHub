package com.g4.odontohub.relacionamentopaciente.recall.domain.repository;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;

import java.util.List;

/** Porta de persistência do agregado Recall (camada de domínio). */
public interface RecallRepository {

    void salvar(Recall recall);

    List<Recall> todos();

    long proximoId();
}
