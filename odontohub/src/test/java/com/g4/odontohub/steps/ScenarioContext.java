package com.g4.odontohub.steps;

import io.cucumber.java.After;

/**
 * Contexto compartilhado entre Step Definitions de diferentes features.
 * Usa ThreadLocal para isolamento de cenários em execução paralela.
 * O hook @After garante limpeza após cada cenário.
 */
public class ScenarioContext {

    private static final ThreadLocal<ScenarioContext> TL =
            ThreadLocal.withInitial(ScenarioContext::new);

    public Exception excecao;

    public static ScenarioContext get() {
        return TL.get();
    }

    @After
    public void reset() {
        TL.remove();
    }
}
