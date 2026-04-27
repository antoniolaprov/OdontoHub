package com.g4.odontohub.steps;

import com.g4.odontohub.estoque.application.EstoqueService;
import com.g4.odontohub.estoque.domain.ItemEstoque;
import com.g4.odontohub.infra.InMemoryItemEstoqueRepository;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para F11 - Controle de Estoque de Materiais Consumíveis.
 *
 * As steps "o dentista registra o procedimento..." são compartilhadas com F13 e
 * implementadas em F13EsterilizacaoSteps, que delega para EstoqueService quando
 * ScenarioContext.consumoEstoque não é nulo.
 *
 * Esta classe popula ScenarioContext com estoqueService e consumoEstoque nos
 * passos @Dado/@Quando de configuração.
 */
public class EstoqueSteps {

    private final InMemoryItemEstoqueRepository repo = new InMemoryItemEstoqueRepository();
    private final EstoqueService service = new EstoqueService(repo);

    // ── Contexto (Background) ─────────────────────────────────────────────────

    @Dado("que o material {string} está cadastrado com saldo de {int} unidades e ponto mínimo de {int}")
    public void materialCadastradoComPontoMinimo(String nome, int saldo, int pontoMinimo) {
        service.cadastrar(nome, saldo, pontoMinimo);
        // Registra o service no contexto para o step compartilhado
        ScenarioContext.get().estoqueService = service;
    }

    // ── Dado: diferentes variantes de saldo ──────────────────────────────────

    @Dado("que o material {string} tem saldo de {int} unidade")
    public void materialComUmaUnidade(String nome, int saldo) {
        service.cadastrar(nome, saldo, null);
        ScenarioContext.get().estoqueService = service;
    }

    @Dado("que o material {string} tem saldo de {int} unidades")
    public void materialComVariasUnidades(String nome, int saldo) {
        service.cadastrar(nome, saldo, null);
        ScenarioContext.get().estoqueService = service;
    }

    @Dado("que o material {string} tem saldo de {int} unidades e ponto mínimo de {int}")
    public void materialComSaldoEPontoMinimo(String nome, int saldo, int pontoMinimo) {
        // Substitui entrada anterior do Background para este material
        repo.removerPorNome(nome);
        service.cadastrar(nome, saldo, pontoMinimo);
        ScenarioContext.get().estoqueService = service;
    }

    @Dado("que o material {string} está cadastrado sem ponto mínimo definido")
    public void materialSemPontoMinimo(String nome) {
        service.cadastrar(nome, 0, null);
        ScenarioContext.get().estoqueService = service;
    }

    // ── Dado: definição de consumo do procedimento ────────────────────────────

    @Dado("que o procedimento {string} utiliza {int} unidades de {string}")
    public void procedimentoUtilizaUnidades(String procedimento, int qtd, String material) {
        Map<String, Integer> consumo = new HashMap<>();
        consumo.put(material, qtd);
        ScenarioContext.get().consumoEstoque = consumo;
        ScenarioContext.get().estoqueService = service;
    }

    @Dado("que o procedimento {string} requer {int} unidades de {string}")
    public void procedimentoRequerUnidades(String procedimento, int qtd, String material) {
        Map<String, Integer> consumo = new HashMap<>();
        consumo.put(material, qtd);
        ScenarioContext.get().consumoEstoque = consumo;
        ScenarioContext.get().estoqueService = service;
    }

    @Dado("que o procedimento {string} utiliza:")
    public void procedimentoUtilizaMultiplosMateriais(String procedimento, DataTable tabela) {
        Map<String, Integer> consumo = new HashMap<>();
        List<Map<String, String>> linhas = tabela.asMaps();
        for (Map<String, String> linha : linhas) {
            String material = linha.get("Material");
            int quantidade = Integer.parseInt(linha.get("Quantidade").trim());
            consumo.put(material, quantidade);
            // Garante que o material existe com saldo suficiente se ainda não cadastrado
            try {
                service.buscarPorNome(material);
            } catch (Exception e) {
                service.cadastrar(material, 100, null);
            }
        }
        ScenarioContext.get().consumoEstoque = consumo;
        ScenarioContext.get().estoqueService = service;
    }

    // ── Quando: ponto mínimo (não conflita com nenhum outro) ─────────────────

    @Quando("o dentista define o ponto mínimo de {string} como {int} unidades")
    public void definirPontoMinimo(String nome, int pontoMinimo) {
        try {
            service.definirPontoMinimoPorNome(nome, pontoMinimo);
        } catch (Exception e) {
            ScenarioContext.get().excecao = e;
        }
    }

    // ── Então ─────────────────────────────────────────────────────────────────

    @Então("o saldo de {string} deve ser reduzido de {int} para {int} unidades automaticamente")
    public void saldoReduzidoDePara(String nome, int saldoAnterior, int saldoEsperado) {
        ItemEstoque item = service.buscarPorNome(nome);
        assertEquals(saldoEsperado, item.getSaldo(),
                "Saldo de " + nome + " incorreto: esperado " + saldoEsperado + ", obtido " + item.getSaldo());
    }

    @Então("o saldo de {string} deve ser reduzido para {int} unidades")
    public void saldoReduzidoPara(String nome, int saldoEsperado) {
        ItemEstoque item = service.buscarPorNome(nome);
        assertEquals(saldoEsperado, item.getSaldo(),
                "Saldo de " + nome + " incorreto");
    }

    @Então("o saldo de {string} deve ser {int}")
    public void saldoDeve(String nome, int saldoEsperado) {
        ItemEstoque item = service.buscarPorNome(nome);
        assertEquals(saldoEsperado, item.getSaldo(),
                "Saldo de " + nome + " incorreto");
    }

    @Então("o sistema deve bloquear o registro")
    public void sistemaBloqueiaRegistro() {
        assertNotNull(ScenarioContext.get().excecao, "Esperava-se uma exceção de bloqueio");
    }

    @Então("o sistema deve emitir um alerta ao dentista informando estoque abaixo do ponto mínimo")
    public void alertaEstoqueMinimo() {
        Map<String, Boolean> alertas = ScenarioContext.get().alertasEstoque;
        assertNotNull(alertas, "Os alertas não foram retornados");
        boolean algumAlerta = alertas.values().stream().anyMatch(a -> a);
        assertTrue(algumAlerta, "Esperava-se alerta de estoque mínimo, mas nenhum foi emitido");
    }

    @Então("nenhum alerta de estoque deve ser emitido")
    public void nenhumAlerta() {
        Map<String, Boolean> alertas = ScenarioContext.get().alertasEstoque;
        if (alertas == null) {
            return; // Nenhum alerta emitido = correto
        }
        boolean algumAlerta = alertas.values().stream().anyMatch(a -> a);
        assertFalse(algumAlerta, "Não deveria ter sido emitido alerta de estoque mínimo");
    }

    @Então("o ponto mínimo de {string} deve ser salvo como {int} unidades")
    public void pontoMinimoSalvo(String nome, int pontoMinimoEsperado) {
        ItemEstoque item = service.buscarPorNome(nome);
        assertNotNull(item.getPontoMinimo(), "Ponto mínimo não foi definido");
        assertEquals(pontoMinimoEsperado, item.getPontoMinimo().intValue(),
                "Ponto mínimo incorreto");
    }

    @Então("todos os materiais devem ter seus saldos reduzidos conforme o consumo")
    public void todosMaterialaisReduzidos() {
        assertNull(ScenarioContext.get().excecao, "Não deveria ter exceção");
        Map<String, Boolean> alertas = ScenarioContext.get().alertasEstoque;
        assertNotNull(alertas, "O procedimento não foi realizado (alertas nulo)");
        Map<String, Integer> consumo = ScenarioContext.get().consumoEstoque;
        for (Map.Entry<String, Integer> entry : consumo.entrySet()) {
            String nome = entry.getKey();
            int consumido = entry.getValue();
            ItemEstoque item = service.buscarPorNome(nome);
            assertNotNull(item, "Material " + nome + " deveria existir");
            // Saldo inicial era 100 (cadastrado em procedimentoUtilizaMultiplosMateriais)
            assertEquals(100 - consumido, item.getSaldo(),
                    "Saldo de " + nome + " incorreto após baixa");
        }
    }
}
