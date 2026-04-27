package com.g4.odontohub.fluxocaixa.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LancamentoCaixaRepository {
    LancamentoCaixa salvar(LancamentoCaixa lancamento);
    Optional<LancamentoCaixa> buscarPorId(String id);
    List<LancamentoCaixa> listarTodos();
    /** Retorna todos os lançamentos com vencimento até a data informada. */
    List<LancamentoCaixa> listarAteData(LocalDate data);
    /** Soma dos valores de entradas previstas até a data informada (inclui parcelas a vencer). */
    BigDecimal somarEntradasPrevisasTe(LocalDate data);
    /** Soma dos valores de saídas previstas até a data informada. */
    BigDecimal somarSaidasPrevistas(LocalDate data);
}
