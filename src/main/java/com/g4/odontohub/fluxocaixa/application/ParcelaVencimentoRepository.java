package com.g4.odontohub.fluxocaixa.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Porta de saída para consulta de parcelas a vencer (usada na projeção de saldo).
 * Implementada como InMemory no módulo de test.
 * AUDITORIA: Sem @Service, @Component ou qualquer anotação Spring aqui.
 */
public interface ParcelaVencimentoRepository {
    /**
     * Retorna a soma dos valores de parcelas com vencimento até a data informada
     * e que ainda não foram liquidadas.
     */
    BigDecimal somarParcelasAVencer(LocalDate dataLimite);

    /**
     * Adiciona uma parcela prevista (usada no setup dos testes).
     */
    void adicionarParcela(LocalDate vencimento, BigDecimal valor);
}
