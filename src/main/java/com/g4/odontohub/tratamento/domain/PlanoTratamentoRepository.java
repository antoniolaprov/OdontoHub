package com.g4.odontohub.tratamento.domain;

public interface PlanoTratamentoRepository {
    void salvar(PlanoTratamento plano);
    PlanoTratamento buscarPorId(Long id);
    void deletar(Long id);
}