package com.g4.odontohub.orcamento.domain;

import java.util.Optional;


public interface OrcamentoRepository {
    void salvar(Orcamento orcamento);
    Orcamento buscarPorId(Long id);
    Optional<Orcamento> buscarPorPlanoTratamentoId(Long planoTratamentoId);
}