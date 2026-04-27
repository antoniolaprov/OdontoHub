package com.g4.odontohub.tratamento.domain;

import java.util.Optional;

/**
 * Porta de repositório do domínio para Tcle.
 * Implementada em infra (InMemoryTcleRepository) nos testes.
 */
public interface TcleRepository {
    void salvar(Tcle tcle);
    Optional<Tcle> buscarPorId(Long id);
    Optional<Tcle> buscarAssinadoPorPlano(Long planoId);
    void deletar(Long id);
}
