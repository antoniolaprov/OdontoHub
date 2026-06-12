package com.g4.odontohub.financeiro.domain.repository;

import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;

import java.util.List;

/** Porta de persistência dos lançamentos do fluxo de caixa (camada de domínio). */
public interface LancamentoRepository {

    void salvar(LancamentoFinanceiro lancamento);

    List<LancamentoFinanceiro> todos();

    long proximoId();
}
