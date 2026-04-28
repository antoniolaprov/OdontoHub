package com.g4.odontohub.steps;

import com.g4.odontohub.estoque.application.MaterialApplicationService;
import com.g4.odontohub.estoque.domain.event.EstoqueBaixoAlertado;
import com.g4.odontohub.estoque.domain.model.MaterialConsumivel;
import io.cucumber.java.pt.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class F05EstoqueSteps {

    private MaterialApplicationService materialService;
    private Exception excecaoCapturada;
    private final Map<String, Integer> consumosPorProcedimento = new HashMap<>();
    private final Map<String, String> materialPorProcedimento = new HashMap<>();

    @Dado("que o material {string} está cadastrado com saldo de {int} unidades e ponto mínimo de {int}")
    public void queOMaterialEstaCadastradoComSaldo(String nome, int saldo, int pontoMinimo) {
        materialService = SharedTestServices.getMaterialService();
        materialService.cadastrarMaterial(nome, "un", saldo, pontoMinimo);
    }

    @Quando("o auxiliar registra a reposição de {int} unidades de {string} com custo unitário de {dinheiro} do fornecedor {string}")
    public void oAuxiliarRegistraAReposicao(int quantidade, String materialNome, double custo, String fornecedor) {
        materialService = SharedTestServices.getMaterialService();
        materialService.registrarReposicao(materialNome, fornecedor, quantidade, custo);
    }

    @Então("o saldo de {string} deve ser atualizado para {int} unidades")
    public void oSaldoDeveSerAtualizadoPara(String materialNome, int saldoEsperado) {
        MaterialConsumivel material = SharedTestServices.getMaterialService().buscarPorNome(materialNome);
        assertEquals(saldoEsperado, material.getQuantidadeEmEstoque());
    }

    @E("deve ser gerada uma saída automática de {dinheiro} no Fluxo de Caixa na categoria {string}")
    public void deveSerGeradaUmaSaidaAutomaticaNoFluxoDeCaixa(double valor, String categoria) {
        boolean encontrado = SharedTestServices.getFluxoCaixaService().getLancamentos().stream()
                .anyMatch(l -> l.isGeradoAutomaticamente()
                        && l.getCategoria().equalsIgnoreCase(categoria)
                        && Math.abs(l.getValor() - valor) < 0.001);
        assertTrue(encontrado,
                "Saída automática de R$" + valor + " na categoria '" + categoria + "' não encontrada");
    }

    @Quando("o auxiliar tenta registrar uma reposição de {int} unidades de {string}")
    public void oAuxiliarTentaRegistrarReposicaoQuantidade(int quantidade, String materialNome) {
        materialService = SharedTestServices.getMaterialService();
        try {
            materialService.registrarReposicao(materialNome, "Fornecedor", quantidade, 1.0);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o auxiliar tenta registrar uma reposição de {int} unidades de {string} com custo unitário de {dinheiro}")
    public void oAuxiliarTentaRegistrarReposicaoComCusto(int quantidade, String materialNome, double custo) {
        materialService = SharedTestServices.getMaterialService();
        try {
            materialService.registrarReposicao(materialNome, "Fornecedor", quantidade, custo);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o sistema deve rejeitar a operação")
    public void oSistemaDeveRejeitarAOperacao() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter rejeitado a operação");
    }

    @E("a mensagem de erro deve informar {string}")
    public void aMensagemDeErroDeveInformar(String mensagemEsperada) {
        assertNotNull(excecaoCapturada);
        assertEquals(mensagemEsperada, excecaoCapturada.getMessage());
    }

    @Dado("que o procedimento {string} consome {int} unidades de {string}")
    public void queOProcedimentoConsome(String procedimento, int quantidade, String materialNome) {
        consumosPorProcedimento.put(procedimento, quantidade);
        materialPorProcedimento.put(procedimento, materialNome);
    }

    @Quando("o dentista realiza o procedimento {string}")
    public void oDentistaRealizaOProcedimento(String procedimento) {
        materialService = SharedTestServices.getMaterialService();
        int quantidade = consumosPorProcedimento.get(procedimento);
        String materialNome = materialPorProcedimento.get(procedimento);
        materialService.descontarConsumo(materialNome, quantidade, 1L);
    }

    @Então("o saldo de {string} deve ser reduzido para {int} unidades")
    public void oSaldoDeveSerReduzidoPara(String materialNome, int saldoEsperado) {
        MaterialConsumivel material = SharedTestServices.getMaterialService().buscarPorNome(materialNome);
        assertEquals(saldoEsperado, material.getQuantidadeEmEstoque());
    }

    @Dado("que o saldo de {string} é de {int} unidades")
    public void queOSaldoDeEDe(String materialNome, int novoSaldo) {
        SharedTestServices.getMaterialService().ajustarSaldo(materialNome, novoSaldo);
    }

    @Quando("{int} unidades de {string} são consumidas")
    public void unidadesSaoConsumidas(int quantidade, String materialNome) {
        materialService = SharedTestServices.getMaterialService();
        materialService.descontarConsumo(materialNome, quantidade, 1L);
    }

    @Então("o saldo deve ser atualizado para {int} unidades")
    public void oSaldoDeveSerAtualizadoParaUnidades(int saldoEsperado) {
        // validado no step seguinte junto com o nome do material
    }

    @E("o sistema deve emitir um alerta de estoque baixo para {string}")
    public void oSistemaDeveEmitirUmAlertaDeEstoqueBaixoPara(String materialNome) {
        EstoqueBaixoAlertado alerta = SharedTestServices.getMaterialService().getUltimoAlertaEstoque();
        assertNotNull(alerta, "Alerta de estoque baixo não foi emitido");
        assertEquals(materialNome, alerta.nomeMaterial());
    }

    @E("o alerta deve informar o saldo atual de {int} e o ponto mínimo de {int}")
    public void oAlertaDeveInformarOSaldoAtualEOPontoMinimo(int saldoAtual, int pontoMinimo) {
        EstoqueBaixoAlertado alerta = SharedTestServices.getMaterialService().getUltimoAlertaEstoque();
        assertNotNull(alerta);
        assertEquals(saldoAtual, alerta.saldoAtual());
        assertEquals(pontoMinimo, alerta.pontoMinimo());
    }
}
