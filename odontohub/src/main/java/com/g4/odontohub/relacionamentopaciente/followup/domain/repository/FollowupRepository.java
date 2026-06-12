package com.g4.odontohub.relacionamentopaciente.followup.domain.repository;

import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupPosOperatorio;

import java.util.List;

/** Porta de persistência do agregado Follow-up (camada de domínio). */
public interface FollowupRepository {

    void salvar(FollowupPosOperatorio tarefa);

    List<FollowupPosOperatorio> todos();

    long proximoId();
}
