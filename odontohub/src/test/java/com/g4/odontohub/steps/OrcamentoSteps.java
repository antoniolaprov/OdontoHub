package com.g4.odontohub.steps;

import com.g4.odontohub.infra.InMemoryOrcamentoRepository;
import com.g4.odontohub.infra.InMemoryPlanoTratamentoRepository;
import com.g4.odontohub.orcamento.application.OrcamentoService;
import com.g4.odontohub.orcamento.domain.ItemOrcamento;
import com.g4.odontohub.orcamento.domain.Orcamento;
import com.g4.odontohub.orcamento.domain.StatusOrcamento;
import com.g4.odontohub.shared.exception.DomainException;
import com.g4.odontohub.tratamento.application.PlanoTratamentoService;
import com.g4.odontohub.tratamento.domain.PlanoTratamento;
import com.g4.odontohub.tratamento.domain.Procedimento;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrcamentoSteps {

    private final InMemoryOrcamentoRepository orcamentoRepo = new InMemoryOrcamentoRepository();
    private final InMemoryPlanoTratamentoRepository planoRepo = new InMemoryPlanoTratamentoRepository();
    private final OrcamentoService orcamentoService = new OrcamentoService(orcamentoRepo, planoRepo);
    private final PlanoTratamentoService planoService = new PlanoTratamentoService(planoRepo, p -> true);

    private Orcamento orcamentoResultado;
    private PlanoTratamento planoResultado;
    private boolean procedimentoBloqueado = false;

    // ── Contexto ─────────────────────────────────────────────────────────────

    @Dado("que o paciente {string} está cadastrado no sistema")
    public void pacienteSetup(String nome) {}

    @Dado("que existe um plano de tratamento ativo para {string} com os procedimentos:")
    public void planoComProcedimentos(String paciente, io.cucumber.datatable.DataTable table) {
        List<Procedimento> procedimentos = table.asMaps().stream()
                .map(row -> {
                    String desc = row.get("Procedimento");
                    return Procedimento.criar(1L, desc);
                })
                .toList();

        planoResultado = planoService.criarPlano(1L, 1L, procedimentos);
    }

    // ── Pré-condições ────────────────────────────────────────────────────────

    @Dado("que o orçamento do plano de {string} tem status {string}")
    public void orcamentoComStatus(String paciente, String status) {
        orcamentoResultado = orcamentoService.gerarComValores(planoResultado.getId());
        if ("APROVADO".equals(status)) {
            orcamentoResultado = orcamentoService.aprobar(orcamentoResultado.getId(), LocalDate.now(), "Teste");
        }
    }

    @Dado("que existe um plano de tratamento com procedimentos cujos valores somam R$ {double}")
    public void planoComValores(double valorTotal) {
        List<Procedimento> procedimentos = List.of(
                Procedimento.criar(1L, "Extração"),
                Procedimento.criar(2L, "Limpeza")
        );
        planoResultado = planoService.criarPlano(1L, 1L, procedimentos);
    }

    // ── Ações ───────────────────────────────────────────────────────────────

    @Quando("o dentista finaliza o plano de tratamento de {string}")
    public void finalizarPlano(String paciente) {
        orcamentoResultado = orcamentoService.gerarComValores(planoResultado.getId());
    }

    @Quando("o dentista tenta marcar o procedimento {string} como {string}")
    public void tentarMarcarProcedimento(String procedimento, String status) {
        try {
            orcamentoService.validarAprovacao(orcamentoResultado.getId());
            procedimentoBloqueado = false;
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
            procedimentoBloqueado = true;
        }
    }

    @Quando("a recepcionista registra a aprovação do orçamento com a forma {string} em {string}")
    public void aprovarOrcamento(String forma, String data) {
        LocalDate dataAprovacao = LocalDate.parse(data);
        orcamentoResultado = orcamentoService.aprobar(orcamentoResultado.getId(), dataAprovacao, forma);
    }

    @Quando("qualquer usuário tenta alterar o valor do procedimento {string}")
    public void tentarAlterarProcedimento(String procedimento) {
        try {
            orcamentoService.alterarItem(orcamentoResultado.getId(), procedimento);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista adiciona o procedimento {string} com valor R$ {double} ao plano em retorno")
    public void adicionarProcedimentoRetorno(String procedimento, double valor) {
        // Primeiro aprova o orçamento atual se ainda não estiver
        if (orcamentoResultado.getStatus() != StatusOrcamento.APROVADO) {
            orcamentoResultado = orcamentoService.aprobar(orcamentoResultado.getId(), LocalDate.now(), "Teste");
        }
        // Gera orçamento complementar
        orcamentoResultado = orcamentoService.gerarComplementar(planoResultado.getId(), BigDecimal.valueOf(valor));
    }

    @Quando("o sistema gera o orçamento")
    public void gerarOrcamento() {
        orcamentoResultado = orcamentoService.gerarComValores(planoResultado.getId());
    }

    // ── Verificações ─────────────────────────────────────────────────────────

    @Então("o sistema deve gerar automaticamente um orçamento")
    public void deveGerarOrcamento() {
        assertNotNull(orcamentoResultado);
        assertNotNull(orcamentoResultado.getId());
    }

    @Então("o valor total do orçamento deve ser R$ {double}")
    public void valorTotalDeveSer(double valor) {
        assertEquals(BigDecimal.valueOf(valor), orcamentoResultado.getValorTotal());
    }

    @Então("o orçamento deve ter status {string}")
    public void orcamentoDeveTerStatus(String status) {
        assertEquals(StatusOrcamento.valueOf(status), orcamentoResultado.getStatus());
    }

    @Então("deve conter os itens {string} e {string} com seus valores unitários")
    public void deveConterItens(String item1, String item2) {
        List<String> descricoes = orcamentoResultado.getItens().stream()
                .map(ItemOrcamento::getDescricao)
                .toList();
        assertTrue(descricoes.contains(item1));
        assertTrue(descricoes.contains(item2));
    }

    @Então("o sistema deve bloquear a alteração")
    public void sistemaBloqueiaAlteracao() {
        assertTrue(procedimentoBloqueado || ScenarioContext.get().excecao != null);
    }

    @Então("a data {string} e a forma {string} devem ser registradas")
    public void dataEFormaRegistradas(String data, String forma) {
        assertEquals(LocalDate.parse(data), orcamentoResultado.getDataAprovacao());
        assertEquals(forma, orcamentoResultado.getFormaAprovacao());
    }

    @Então("o sistema deve rejeitar a alteração")
    public void sistemaRejeitaAlteracao() {
        assertNotNull(ScenarioContext.get().excecao);
    }

    @Então("o sistema deve gerar um novo orçamento complementar com valor R$ {double}")
    public void orcamentoComplementarGerado(double valor) {
        assertNotNull(orcamentoResultado);
        assertTrue(orcamentoResultado.isComplementar());
        assertEquals(BigDecimal.valueOf(valor), orcamentoResultado.getValorTotal());
    }

    @Então("o orçamento complementar deve ter o campo {string} marcado como verdadeiro")
    public void campoComplementarVerdadeiro(String campo) {
        assertTrue(orcamentoResultado.isComplementar());
    }

    @Então("o orçamento original deve permanecer inalterado")
    public void orcamentoOriginalInalterado() {
        Orcamento original = orcamentoService.buscarPorPlano(planoResultado.getId());
        assertNotNull(original);
        assertEquals(StatusOrcamento.APROVADO, original.getStatus());
    }

    @Então("o valor total do orçamento deve ser exatamente R$ {double}")
    public void valorTotalExato(double valor) {
        assertEquals(BigDecimal.valueOf(valor), orcamentoResultado.getValorTotal());
    }
}