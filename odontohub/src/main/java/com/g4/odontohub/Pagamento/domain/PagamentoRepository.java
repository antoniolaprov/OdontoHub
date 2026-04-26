package com.g4.odontohub.Pagamento.domain;

public interface PagamentoRepository {
    void salvar(Pagamento pagamento);
    boolean existePorPacienteId(String pacienteId);
}