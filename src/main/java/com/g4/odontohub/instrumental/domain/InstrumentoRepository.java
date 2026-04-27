package com.g4.odontohub.instrumental.domain;

public interface InstrumentoRepository {
    void salvar(Instrumento instrumento);
    Instrumento buscarPorNome(String nome);
}