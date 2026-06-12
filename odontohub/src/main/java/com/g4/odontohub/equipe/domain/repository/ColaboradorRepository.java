package com.g4.odontohub.equipe.domain.repository;

import com.g4.odontohub.equipe.domain.model.Colaborador;

import java.util.List;

/** Porta de persistência do contexto de Equipe (camada de domínio). */
public interface ColaboradorRepository {

    void salvar(Colaborador colaborador);

    Colaborador buscarPorNome(String nomeCompleto);

    List<Colaborador> todos();

    long proximoId();
}
