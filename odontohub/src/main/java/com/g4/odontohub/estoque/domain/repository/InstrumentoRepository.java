package com.g4.odontohub.estoque.domain.repository;

import com.g4.odontohub.estoque.domain.model.Instrumento;

import java.util.List;

/** Porta de persistência de instrumentos (camada de domínio). */
public interface InstrumentoRepository {

    void salvar(Instrumento instrumento);

    Instrumento buscarPorId(Long id);

    Instrumento buscarPorNome(String nome);

    Instrumento buscarPorCodigo(String codigo);

    List<Instrumento> todos();

    long proximoId();
}
