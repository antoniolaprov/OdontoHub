package com.g4.odontohub.steps;

import com.g4.odontohub.infra.InMemoryMaterialRepository;
import com.g4.odontohub.material.application.MaterialService;
import com.g4.odontohub.material.domain.Material;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MaterialSteps {

    // Repositório instanciado apenas para ser injetado no Service
    private final MaterialService service = new MaterialService(new InMemoryMaterialRepository());

    private String materialId;
    private int quantidadeReposicao;

    @Dado("que existe um material {string} com {int} unidades no estoque")
    public void que_existe_um_material_com_unidades(String nome, int qtdInicial) {
        // Chamada via Service, não via repositório
        this.materialId = service.cadastrar(nome, qtdInicial);
    }

    @Quando("eu realizo a reposição de {int} unidades")
    public void eu_realizo_a_reposicao_de_unidades(int qtd) {
        this.quantidadeReposicao = qtd;
        try {
            service.repor(materialId, quantidadeReposicao);
        } catch (Exception e) {
            // Salva a exceção diretamente na variável pública da sua classe ScenarioContext (mesmo pacote)
            ScenarioContext.get().excecao = e;
        }
    }

    @Entao("o estoque do material deve ser {int} unidades")
    public void o_estoque_do_material_deve_ser(int qtdEsperada) {
        // Verificação via Service
        Material material = service.buscar(materialId);
        assertNotNull(material, "Material deveria existir");
        assertEquals(qtdEsperada, material.getQuantidadeEmEstoque(), "A quantidade em estoque não confere");
    }
}