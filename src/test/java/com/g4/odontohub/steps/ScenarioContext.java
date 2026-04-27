package com.g4.odontohub.steps;

import com.g4.odontohub.estoque.application.EstoqueService;

import io.cucumber.java.After;

import java.util.Map;

/**
 * Contexto compartilhado entre Step Definitions de diferentes features.
 * Usa ThreadLocal para isolamento de cenários em execução paralela.
 * O hook @After garante limpeza após cada cenário.
 */
public class ScenarioContext {

    private static final ThreadLocal<ScenarioContext> TL =
            ThreadLocal.withInitial(ScenarioContext::new);

    /** Exceção capturada pelo step Quando/Então que esperava falha */
    public Exception excecao;

    // ── Contexto de Estoque (F11) ────────────────────────────────────────────
    /** Service de estoque usado pelo step compartilhado de procedimento */
    public EstoqueService estoqueService;
    /** Consumo de materiais do procedimento atual */
    public Map<String, Integer> consumoEstoque;
    /** Resultado dos alertas de estoque mínimo */
    public Map<String, Boolean> alertasEstoque;

    public static ScenarioContext get() {
        return TL.get();
    }

    @After
    public void reset() {
        TL.remove();
    }
}
