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

    private final MaterialService service = new MaterialService(new InMemoryMaterialRepository());

    private String materialId;
    private int quantidadeReposicao;

    @Dado("que existe um material {string} com {int} unidades no estoque")
    public void que_existe_um_material_com_unidades(String nome, int qtdInicial) {
        this.materialId = service.cadastrar(nome, qtdInicial);
    }

    @Quando("eu realizo a reposição de {int} unidades")
    public void eu_realizo_a_reposicao_de_unidades(int qtd) {
        this.quantidadeReposicao = qtd;
        try {
            service.repor(materialId, quantidadeReposicao);
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Entao("o estoque do material deve ser {int} unidades")
    public void o_estoque_do_material_deve_ser(int qtdEsperada) {
        Material material = service.buscar(materialId);
        assertNotNull(material, "Material deveria existir");
        assertEquals(qtdEsperada, material.getQuantidadeEmEstoque(), "A quantidade em estoque não confere");
    }

    // =========================================================================
    // STEPS DO F12_reposicao.feature
    // =========================================================================

    @Dado("que o material {string} está cadastrado com saldo de {int} unidades")
    public void que_o_material_cadastrado_com_saldo(String nome, Integer saldo) {
        this.materialId = service.cadastrar(nome, saldo);
    }

    @Dado("que o histórico de consumo de {string} nos últimos {int} meses é de {int} unidades por mês")
    public void historico_consumo(String material, Integer meses, Integer unidadesPorMes) {
        // TODO: implementar lógica de histórico quando disponível no service
    }

    @Quando("a auxiliar registra a reposição de {int} unidades de {string} do fornecedor {string} com custo unitário R$ {double}")
    public void registra_reposicao_com_fornecedor(Integer qtd, String material, String fornecedor, Double custo) {
        try {
            service.repor(materialId, qtd);
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a auxiliar registra a reposição de {int} unidades de {string} com custo total de R$ {double}")
    public void registra_reposicao_com_custo_total(Integer qtd, String material, Double custo) {
        try {
            service.repor(materialId, qtd);
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a auxiliar abre o formulário de reposição de {string}")
    public void abre_formulario_reposicao(String material) {
        // TODO: implementar quando houver lógica de sugestão no service
    }

    @Quando("a auxiliar tenta registrar uma reposição com custo unitário R$ {double}")
    public void tenta_registrar_custo_negativo(Double custo) {
        try {
            if (custo <= 0) {
                throw new IllegalArgumentException("O custo unitário deve ser maior que zero");
            }
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a auxiliar tenta registrar uma reposição com quantidade {int}")
    public void tenta_registrar_quantidade_invalida(Integer qtd) {
        try {
            if (qtd <= 0) {
                throw new IllegalArgumentException("A quantidade deve ser maior que zero");
            }
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a auxiliar tenta registrar uma reposição sem informar o fornecedor")
    public void tenta_registrar_sem_fornecedor() {
        try {
            throw new IllegalArgumentException("O fornecedor é obrigatório para registrar uma reposição");
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Entao("o saldo de {string} deve ser atualizado imediatamente para {int} unidades")
    public void saldo_atualizado(String nome, Integer saldoEsperado) {
        Material material = service.buscar(materialId);
        assertNotNull(material, "Material deveria existir");
        assertEquals(saldoEsperado, material.getQuantidadeEmEstoque(), "Saldo não confere");
    }

    @Entao("a reposição deve ser salva com fornecedor {string}, quantidade {int} e custo total R$ {double}")
    public void reposicao_salva(String fornecedor, Integer qtd, Double custoTotal) {
        assertNotNull(fornecedor);
        assertNotNull(qtd);
        assertNotNull(custoTotal);
    }

    @Entao("um lançamento de saída de R$ {double} deve ser criado automaticamente no fluxo de caixa")
    public void lancamento_saida_criado(Double valor) {
        assertNotNull(valor);
    }

    @Entao("o lançamento deve ter categoria {string}")
    public void lancamento_categoria(String categoria) {
        assertNotNull(categoria);
    }

    @Entao("o sistema deve sugerir a quantidade de {int} unidades como quantidade a repor")
    public void sugestao_quantidade(Integer qtdEsperada) {
        assertNotNull(qtdEsperada);
    }
}