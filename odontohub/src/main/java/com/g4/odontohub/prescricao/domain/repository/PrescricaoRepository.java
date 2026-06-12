package com.g4.odontohub.prescricao.domain.repository;

import com.g4.odontohub.prescricao.domain.model.Prescricao;

import java.util.List;

/** Porta de persistência do contexto de Prescrição (camada de domínio). */
public interface PrescricaoRepository {

    void salvar(Prescricao prescricao);

    Prescricao buscar(Long id);

    List<Prescricao> todos();

    long proximoId();
}
