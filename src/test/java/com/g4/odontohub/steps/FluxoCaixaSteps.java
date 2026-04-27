package com.g4.odontohub.steps;

import com.g4.odontohub.fluxocaixa.application.FluxoCaixaService;
import com.g4.odontohub.fluxocaixa.application.ProjecaoSaldo;
import com.g4.odontohub.fluxocaixa.domain.LancamentoCaixa;
import com.g4.odontohub.infra.InMemoryLancamentoCaixaRepository;
import com.g4.odontohub.infra.InMemoryParcelaVencimentoRepository;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para F09 - Fluxo de Caixa do Consultório.
 * Cucumber instancia esta classe uma vez por scenario.
 */
public class FluxoCaixaSteps {

    private final InMemoryLancamentoCaixaRepository lancamentoRepo = new InMemoryLancamentoCaixaRepository();
    private final InMemoryParcelaVencimentoRepository parcelaRepo = new InMemoryParcelaVencimentoRepository();
    private final FluxoCaixaService service = new FluxoCaixaService(lancamentoRepo, parcelaRepo);

    private LancamentoCaixa lancamentoAtual;
    private ProjecaoSaldo projecao;
    private String parcelaId;
    private BigDecimal valorParcela;

    // ── Contexto (Background) ─────────────────────────────────────────────────

    @Dado("que o sistema possui lançamentos financeiros registrados")
    public void sistemaComLancamentos() {
        // Pré-condição genérica — estado concreto criado pelos steps específicos de cada cenário
    }

    // ── Cenário 1: Gerar entrada automática ao liquidar parcela ─────────────

    @Dado("que a parcela {int} do paciente {string} no valor de R$ {double} está com status {string}")
    public void parcelaComStatus(int numeroParcela, String paciente, double valor, String status) {
        this.parcelaId = "parcela-" + numeroParcela + "-" + paciente.replaceAll("\\s", "");
        this.valorParcela = BigDecimal.valueOf(valor);
    }

    @Quando("a recepcionista liquida a parcela {int}")
    public void liquidarParcela(int numeroParcela) {
        try {
            lancamentoAtual = service.liquidarParcela(parcelaId, valorParcela);
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("um lançamento de entrada de R$ {double} deve ser criado automaticamente no fluxo de caixa")
    public void lancamentoEntradaCriado(double valorEsperado) {
        assertNotNull(lancamentoAtual, "O lançamento não foi criado");
        assertEquals(0, BigDecimal.valueOf(valorEsperado).compareTo(lancamentoAtual.getValor()),
                "Valor do lançamento incorreto");
    }

    @Então("o lançamento deve ter o campo {string} como verdadeiro")
    public void lancamentoGeradoAutomaticamente(String campo) {
        assertNotNull(lancamentoAtual);
        assertTrue(lancamentoAtual.isGeradoAutomaticamente(),
                "O campo geradoAutomaticamente deveria ser verdadeiro");
    }

    @Então("o lançamento deve referenciar a parcela liquidada como origem")
    public void lancamentoReferenciaParcelaOrigem() {
        assertNotNull(lancamentoAtual);
        assertNotNull(lancamentoAtual.getParcelaOrigemId(),
                "A parcela de origem deveria estar referenciada");
        assertEquals(parcelaId, lancamentoAtual.getParcelaOrigemId());
    }

    // ── Cenário 2: Impedir edição de entrada gerada automaticamente ──────────

    @Dado("que existe um lançamento de entrada gerado automaticamente por liquidação de parcela")
    public void lancamentoEntradaAutomaticoExistente() {
        this.parcelaId = "parcela-auto-1";
        this.valorParcela = new BigDecimal("200.00");
        lancamentoAtual = service.liquidarParcela(parcelaId, valorParcela);
    }

    @Quando("qualquer usuário tenta editar o valor do lançamento")
    public void tentaEditarLancamento() {
        try {
            service.editarValor(lancamentoAtual.getId(), new BigDecimal("999.00"));
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    // ── Cenário 3: Registrar saída avulsa ────────────────────────────────────

    @Quando("o dentista registra uma saída de R$ {double} na categoria {string} com a justificativa {string}")
    public void registrarSaidaAvulsa(double valor, String categoria, String justificativa) {
        try {
            lancamentoAtual = service.registrarSaidaAvulsa(BigDecimal.valueOf(valor), categoria, justificativa);
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("o lançamento de saída deve ser criado com valor R$ {double}")
    public void lancamentoSaidaCriadoComValor(double valorEsperado) {
        assertNotNull(lancamentoAtual, "O lançamento de saída não foi criado");
        assertEquals(0, BigDecimal.valueOf(valorEsperado).compareTo(lancamentoAtual.getValor()),
                "Valor da saída incorreto");
    }

    @Então("a categoria {string} e a justificativa devem ser registradas")
    public void categoriaEJustificativaRegistradas(String categoriaEsperada) {
        assertNotNull(lancamentoAtual);
        assertEquals(categoriaEsperada, lancamentoAtual.getCategoria(), "Categoria incorreta");
        assertNotNull(lancamentoAtual.getJustificativa(), "Justificativa deveria estar preenchida");
        assertFalse(lancamentoAtual.getJustificativa().isBlank(), "Justificativa não pode ser vazia");
    }

    // ── Cenário 4: Bloquear saída avulsa sem justificativa ───────────────────

    @Quando("o dentista tenta registrar uma saída sem informar a justificativa")
    public void tentaRegistrarSaidaSemJustificativa() {
        try {
            service.registrarSaidaAvulsa(new BigDecimal("100.00"), "Outros", null);
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    // ── Cenário 5: Calcular saldo projetado ──────────────────────────────────

    @Dado("que existem as seguintes parcelas a vencer:")
    public void parcelasAVencer(DataTable tabela) {
        List<Map<String, String>> linhas = tabela.asMaps();
        for (Map<String, String> linha : linhas) {
            LocalDate vencimento = LocalDate.parse(linha.get("Vencimento"));
            // Valor pode estar no formato "300,00" (brasileiro)
            String valorStr = linha.get("Valor").replace(",", ".");
            BigDecimal valor = new BigDecimal(valorStr);
            parcelaRepo.adicionarParcela(vencimento, valor);
        }
    }

    @Dado("existe uma saída prevista de R$ {double}")
    public void saidaPrevista(double valor) {
        service.registrarSaidaAvulsa(BigDecimal.valueOf(valor), "Planejado", "Saída prevista para projeção");
    }

    @Quando("o dentista solicita a projeção de saldo até {string}")
    public void solicitarProjecao(String dataStr) {
        LocalDate dataLimite = LocalDate.parse(dataStr);
        projecao = service.projetarSaldo(dataLimite);
    }

    @Então("o saldo projetado deve considerar entradas de R$ {double} e saídas de R$ {double}")
    public void projecaoEntradasESaidas(double entradasEsperadas, double saidasEsperadas) {
        assertNotNull(projecao, "Projeção não foi calculada");
        assertEquals(0, BigDecimal.valueOf(entradasEsperadas).compareTo(projecao.getEntradas()),
                "Entradas da projeção incorretas");
        assertEquals(0, BigDecimal.valueOf(saidasEsperadas).compareTo(projecao.getSaidas()),
                "Saídas da projeção incorretas");
    }

    @Então("o saldo projetado deve ser R$ {double}")
    public void saldoProjetado(double saldoEsperado) {
        assertNotNull(projecao, "Projeção não foi calculada");
        assertEquals(0, BigDecimal.valueOf(saldoEsperado).compareTo(projecao.getSaldo()),
                "Saldo projetado incorreto: esperado " + saldoEsperado + ", obtido " + projecao.getSaldo());
    }

    // ── Cenário 6: Saída automática por reposição de estoque ─────────────────

    @Dado("que uma reposição de {string} com quantidade {int} e custo unitário R$ {double} foi registrada")
    public void reposicaoRegistrada(String material, int quantidade, double custoUnitario) {
        BigDecimal total = BigDecimal.valueOf(custoUnitario).multiply(BigDecimal.valueOf(quantidade));
        try {
            lancamentoAtual = service.registrarSaidaAutomatica(total, "Insumos",
                    "Reposição automática de " + material + " (" + quantidade + " unidades)");
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("um lançamento de saída de R$ {double} deve ser criado automaticamente na categoria {string}")
    public void lancamentoSaidaAutomaticoCriado(double valorEsperado, String categoriaEsperada) {
        assertNotNull(lancamentoAtual, "O lançamento de saída automático não foi criado");
        assertEquals(0, BigDecimal.valueOf(valorEsperado).compareTo(lancamentoAtual.getValor()),
                "Valor da saída automática incorreto");
        assertEquals(categoriaEsperada, lancamentoAtual.getCategoria(),
                "Categoria da saída automática incorreta");
    }
}
