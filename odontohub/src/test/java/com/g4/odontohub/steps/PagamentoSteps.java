package com.g4.odontohub.steps;

import com.g4.odontohub.infra.InMemoryPagamentoRepository;
import com.g4.odontohub.Pagamento.application.PagamentoService;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PagamentoSteps {

    // Repositório instanciado apenas para ser injetado no Service
    private final PagamentoService service = new PagamentoService(new InMemoryPagamentoRepository());

    private String pacienteId;
    private double valorPagamento;

    @Dado("que possuo os dados de pagamento do paciente {string} no valor de {double}")
    public void que_possuo_os_dados_de_pagamento(String pacienteId, double valor) {
        this.pacienteId = pacienteId;
        this.valorPagamento = valor;
    }

    @Quando("eu registro o pagamento")
    public void eu_registro_o_pagamento() {
        try {
            service.registrar(pacienteId, valorPagamento);
        } catch (Exception e) {
            // Salva a exceção diretamente na variável pública da sua classe ScenarioContext (mesmo pacote)
            ScenarioContext.get().excecao = e;
        }
    }

    @Entao("o pagamento deve constar como concluído no sistema")
    public void o_pagamento_deve_constar_como_concluido() {
        // Verificação via Service
        boolean existe = service.verificarSeExistePagamento(pacienteId);
        assertTrue(existe, "Pagamento não encontrado via serviço");
    }
}