package com.g4.odontohub.inadimplencia.domain;

import java.util.List;
import java.util.Optional;

/**
 * Porta de repositório para PagamentoParcelado.
 */
public interface PagamentoParceladoRepository {
    void salvar(PagamentoParcelado pagamento);
    Optional<PagamentoParcelado> buscarPorPaciente(String nomePaciente);
    List<PagamentoParcelado> listarTodos();
}
