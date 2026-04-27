package com.g4.odontohub.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;

import static org.junit.jupiter.api.Assertions.*;

public class SharedSteps {

    @Dado("que o dentista {string} está cadastrado no sistema")
    public void dentistaSetup(String nome) {}

    @Dado("que o paciente {string} está cadastrado no sistema")
    public void pacienteSetup(String nome) {}

    @Então("o sistema deve rejeitar o registro")
    public void sistemaRejeitaRegistro() {
        assertNotNull(ScenarioContext.get().excecao);
    }

    @Então("o sistema deve rejeitar a edição")
    public void sistemaRejeitaEdicao() {
        assertNotNull(ScenarioContext.get().excecao);
    }

    @Então("o sistema deve rejeitar a exclusão")
    public void sistemaRejeitaExclusao() {
        assertNotNull(ScenarioContext.get().excecao);
    }

    @Então("exibir a mensagem {string}")
    public void exibirMensagem(String mensagem) {
        Exception ex = ScenarioContext.get().excecao;
        assertNotNull(ex, "Esperava-se uma exceção com a mensagem: " + mensagem);
        assertTrue(ex.getMessage().contains(mensagem),
                "Mensagem esperada: '" + mensagem + "' | Mensagem obtida: '" + ex.getMessage() + "'");
    }

    @Então("a mensagem deve informar {string}")
    public void mensagemDeveInformar(String mensagem) {
        exibirMensagem(mensagem);
    }
}
