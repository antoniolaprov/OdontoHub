package com.g4.odontohub.steps;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import com.g4.odontohub.financeiro.domain.model.Parcela;
import io.cucumber.java.pt.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class F04FluxoCaixaSteps {

    private FluxoCaixaApplicationService fluxoCaixaService;
    private Parcela parcelaAtual;
    private LancamentoFinanceiro lancamentoAtual;
    private double saldoProjetado;
    private int pontoDeEquilibrio;
    private double custoFixo;
    private double valorMedio;
    private String reposicaoMaterial;
    private int reposicaoQuantidade;
    private double reposicaoCusto;

    @Dado("que existe uma parcela de {dinheiro} com status {string}")
    public void queExisteUmaParcelaDe(double valor, String status) {
        fluxoCaixaService = SharedTestServices.getFluxoCaixaService();
        parcelaAtual = fluxoCaixaService.criarParcela(valor);
    }

    @Quando("a parcela é liquidada")
    public void aParcelaELiquidada() {
        lancamentoAtual = fluxoCaixaService.liquidarParcela(parcelaAtual);
    }

    @Então("deve ser gerado automaticamente um lançamento de entrada no Fluxo de Caixa de {dinheiro}")
    public void deveSerGeradoAutomaticamenteUmLancamentoDeEntrada(double valor) {
        assertNotNull(lancamentoAtual, "Lançamento não foi gerado");
        assertEquals(valor, lancamentoAtual.getValor(), 0.001);
    }

    @E("o lançamento deve ser marcado como gerado automaticamente")
    public void oLancamentoDeveSerMarcadoComoGeradoAutomaticamente() {
        LancamentoFinanceiro l = fluxoCaixaService.getUltimoLancamento();
        assertTrue(l.isGeradoAutomaticamente(), "Lançamento deveria ser gerado automaticamente");
    }

    @E("não deve ser possível editar esse lançamento manualmente")
    public void naoDeveSerPossivelEditarEsseLancamentoManualmente() {
        LancamentoFinanceiro l = fluxoCaixaService.getUltimoLancamento();
        assertThrows(IllegalStateException.class,
                () -> l.editar(999.0, "categoria", "descricao"),
                "Deveria lançar exceção ao editar lançamento automático");
    }

    @Quando("o dentista registra uma saída manual de {dinheiro} na categoria {string} com descrição {string}")
    public void oDentistaRegistraUmaSaidaManual(double valor, String categoria, String descricao) {
        fluxoCaixaService = SharedTestServices.getFluxoCaixaService();
        lancamentoAtual = fluxoCaixaService.registrarSaidaManual(valor, categoria, descricao);
    }

    @Então("o lançamento de saída deve ser registrado no Fluxo de Caixa")
    public void oLancamentoDeSaidaDeveSerRegistrado() {
        assertNotNull(lancamentoAtual, "Lançamento de saída não foi registrado");
    }

    @E("o lançamento deve permitir edição futura")
    public void oLancamentoDevePermitirEdicaoFutura() {
        assertDoesNotThrow(() -> lancamentoAtual.editar(150.0, "Nova categoria", "Nova descrição"),
                "Lançamento manual deveria permitir edição");
    }

    @Dado("que as entradas confirmadas do mês somam {dinheiro}")
    public void queAsEntradasConfirmadasDoMesSomam(double valor) {
        fluxoCaixaService = SharedTestServices.getFluxoCaixaService();
        fluxoCaixaService.registrarEntradaAutomatica(valor, "Entradas confirmadas", LocalDate.now());
    }

    @E("que as saídas previstas do mês somam {dinheiro}")
    public void queAsSaidasPrevistasDoMesSomam(double valor) {
        fluxoCaixaService.registrarSaidaManual(valor, "Saídas previstas", "Saídas do mês");
    }

    @Quando("o saldo projetado é calculado")
    public void oSaldoProjetadoECalculado() {
        saldoProjetado = fluxoCaixaService.calcularSaldoProjetado();
    }

    @Então("o sistema deve emitir um alerta de fluxo negativo")
    public void oSistemaDeveEmitirUmAlertaDeFluxoNegativo() {
        assertNotNull(fluxoCaixaService.getUltimoAlerta(), "Alerta de fluxo negativo não foi emitido");
    }

    @E("o saldo projetado deve ser de {dinheiro}")
    public void oSaldoProjetadoDeveSerDe(double valor) {
        assertEquals(valor, saldoProjetado, 0.001);
    }

    @Dado("que os custos fixos mensais do consultório são de {dinheiro}")
    public void queOsCustosFixosMensaisDoConsultorioSaoDe(double valor) {
        fluxoCaixaService = SharedTestServices.getFluxoCaixaService();
        custoFixo = valor;
    }

    @E("que o valor médio por procedimento é de {dinheiro}")
    public void queOValorMedioPorProcedimentoEDe(double valor) {
        valorMedio = valor;
    }

    @Quando("o ponto de equilíbrio é calculado")
    public void oPontoDeEquilibrioECalculado() {
        pontoDeEquilibrio = fluxoCaixaService.calcularPontoDeEquilibrio(custoFixo, valorMedio);
    }

    @Então("o sistema deve informar que são necessários {int} procedimentos para cobrir os custos fixos")
    public void oSistemaDeveInformarQuantosProcedimentos(int quantidade) {
        assertEquals(quantidade, pontoDeEquilibrio);
    }

    @Dado("que o material {string} é reposto com {int} unidades a {dinheiro} cada")
    public void queOMaterialERepostoComUnidadesACada(String materialNome, int quantidade, double custo) {
        fluxoCaixaService = SharedTestServices.getFluxoCaixaService();
        SharedTestServices.getMaterialService().cadastrarMaterial(materialNome, "un", 0, 5);
        reposicaoMaterial = materialNome;
        reposicaoQuantidade = quantidade;
        reposicaoCusto = custo;
    }

    @Quando("a reposição é registrada")
    public void aReposicaoERegistrada() {
        SharedTestServices.getMaterialService()
                .registrarReposicao(reposicaoMaterial, "Fornecedor", reposicaoQuantidade, reposicaoCusto);
    }

    @Então("deve ser gerado automaticamente um lançamento de saída de {dinheiro} na categoria {string}")
    public void deveSerGeradoAutomaticamenteUmLancamentoDeSaida(double valor, String categoria) {
        boolean encontrado = fluxoCaixaService.getLancamentos().stream()
                .anyMatch(l -> l.isGeradoAutomaticamente()
                        && l.getCategoria().equalsIgnoreCase(categoria)
                        && Math.abs(l.getValor() - valor) < 0.001);
        assertTrue(encontrado,
                "Lançamento automático de saída de R$" + valor + " na categoria '" + categoria + "' não encontrado");
    }
}
