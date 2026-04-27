package com.g4.odontohub.steps;

import com.g4.odontohub.infra.InMemoryPlanoTratamentoRepository;
import com.g4.odontohub.infra.InMemoryTcleRepository;
import com.g4.odontohub.shared.exception.DomainException;
import com.g4.odontohub.tratamento.application.PlanoTratamentoService;
import com.g4.odontohub.tratamento.application.ProntuarioACL;
import com.g4.odontohub.tratamento.application.TcleService;
import com.g4.odontohub.tratamento.domain.Procedimento;
import com.g4.odontohub.tratamento.domain.StatusTcle;
import com.g4.odontohub.tratamento.domain.Tcle;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para F04 - Registro do TCLE.
 * Chama exclusivamente TcleService e PlanoTratamentoService (camada application).
 * Nunca acessa repositório diretamente.
 */
public class F04TcleSteps {

    private static final Long PLANO_ID = 200L;
    private static final Long PACIENTE_ID = 2L;
    private static final Long TCLE_ID_INICIAL = 1L;

    private InMemoryPlanoTratamentoRepository planoRepository;
    private InMemoryTcleRepository tcleRepository;
    private PlanoTratamentoService planoService;
    private TcleService tcleService;

    // Referências para asserções
    private Tcle tcleAtual;
    private Tcle novoTcle;
    private Long tcleAntigoId;

    @Before
    public void setup() {
        planoRepository = new InMemoryPlanoTratamentoRepository();
        tcleRepository = new InMemoryTcleRepository();
        ProntuarioACL mockACL = pacienteId -> true; // anamnese sempre presente no contexto F04
        planoService = new PlanoTratamentoService(planoRepository, mockACL);
        tcleService = new TcleService(tcleRepository, planoRepository);
    }

    // ── Contexto ─────────────────────────────────────────────────────────────

    /**
     * Contexto: cria o plano de tratamento ativo com um procedimento PENDENTE.
     * Step específico do F04 para não conflitar com outros features.
     */
    @Dado("que existe um plano de tratamento ativo para {string}")
    public void existePlanoAtivoParaPaciente(String nomePaciente) {
        // Garante que o plano existe (idempotente: se já existe, não recria)
        if (planoRepository.buscarPorId(PLANO_ID) == null) {
            Procedimento proc = Procedimento.criar(1L, "Extração");
            planoService.criarPlano(PLANO_ID, PACIENTE_ID, List.of(proc));
        }
    }

    // ── Dados (Dado) ──────────────────────────────────────────────────────────

    @Dado("que não existe TCLE assinado vinculado ao plano de tratamento de {string}")
    public void naoExisteTcleAssinado(String nomePaciente) {
        // Nenhum TCLE no repositório — nada a fazer; o repositório começa vazio
    }

    @Dado("que existe um TCLE com status {string} para {string}")
    public void existeTcleComStatus(String status, String nomePaciente) {
        Tcle tcle = Tcle.criar(TCLE_ID_INICIAL, PLANO_ID, nomePaciente);
        if ("ASSINADO".equals(status)) {
            tcle.assinar(LocalDate.of(2026, 5, 1));
        }
        tcleRepository.salvar(tcle);
        tcleAtual = tcle;
    }

    @Dado("que existe um TCLE com status {string} vinculado ao plano de {string}")
    public void existeTcleAssinadoVinculadoAoPlano(String status, String nomePaciente) {
        existeTcleComStatus(status, nomePaciente);
    }

    // ── Ações (Quando) ────────────────────────────────────────────────────────

    @Quando("o dentista tenta marcar um procedimento do plano como {string}")
    public void tentaMarcarProcedimentoComoRealizado(String statusDesejado) {
        try {
            tcleService.tentarRealizarProcedimentoSemTcle(PLANO_ID, "Extração");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista registra o TCLE como assinado por {string} em {string}")
    public void registraTcleAssinado(String nomePaciente, String dataStr) {
        try {
            LocalDate data = LocalDate.parse(dataStr);
            tcleAtual = tcleService.registrarAssinado(PLANO_ID, nomePaciente, data);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista tenta registrar um TCLE sem informar o plano de tratamento")
    public void tentaRegistrarTcleSemPlano() {
        try {
            tcleService.registrarSemPlano("Ana Ferreira");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("qualquer usuário tenta excluir o TCLE")
    public void tentaExcluirTcle() {
        try {
            tcleService.excluirTcle(tcleAtual.getId());
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista substitui o TCLE com a justificativa {string}")
    public void substituiTcle(String justificativa) {
        try {
            tcleAntigoId = tcleAtual.getId();
            novoTcle = tcleService.substituir(tcleAntigoId, justificativa);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista marca um procedimento do plano como {string}")
    public void marcaProcedimentoComoRealizado(String statusDesejado) {
        try {
            tcleService.realizarProcedimento(PLANO_ID, "Extração");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    // ── Verificações (Então) ──────────────────────────────────────────────────

    @Então("o sistema deve bloquear a alteração de status")
    public void sistemaBloqueiaAlteracaoStatus() {
        assertNotNull(ScenarioContext.get().excecao,
                "Esperava-se bloqueio ao tentar realizar procedimento sem TCLE assinado");
    }

    @Então("o TCLE deve ser salvo com status {string}")
    public void tcleSalvoComStatus(String statusEsperado) {
        assertNull(ScenarioContext.get().excecao,
                () -> "Erro inesperado: " + ScenarioContext.get().excecao.getMessage());
        assertNotNull(tcleAtual, "TCLE não foi criado");
        assertEquals(StatusTcle.valueOf(statusEsperado), tcleAtual.getStatus());
    }

    @Então("deve estar vinculado ao plano de tratamento correspondente")
    public void tcleVinculadoAoPlano() {
        assertNotNull(tcleAtual);
        assertEquals(PLANO_ID, tcleAtual.getPlanoId());
    }

    @Então("o TCLE antigo deve ter status alterado para {string}")
    public void tcleAntigoComStatus(String statusEsperado) {
        Tcle tcleAntigo = tcleRepository.buscarPorId(tcleAntigoId)
                .orElseThrow(() -> new AssertionError("TCLE antigo não encontrado no repositório"));
        assertEquals(StatusTcle.valueOf(statusEsperado), tcleAntigo.getStatus());
    }

    @Então("um novo TCLE deve ser criado com status {string}")
    public void novoTcleComStatus(String statusEsperado) {
        assertNotNull(novoTcle, "Novo TCLE não foi criado");
        assertEquals(StatusTcle.valueOf(statusEsperado), novoTcle.getStatus());
    }

    @Então("a justificativa de substituição deve ser registrada como {string}")
    public void justificativaSubstituicaoRegistrada(String justificativaEsperada) {
        Tcle tcleAntigo = tcleRepository.buscarPorId(tcleAntigoId)
                .orElseThrow(() -> new AssertionError("TCLE antigo não encontrado"));
        assertEquals(justificativaEsperada, tcleAntigo.getJustificativaSubstituicao());
    }

    @Então("o procedimento deve ter seu status alterado para {string}")
    public void procedimentoStatusAlterado(String statusEsperado) {
        assertNull(ScenarioContext.get().excecao,
                () -> "Erro inesperado: " + ScenarioContext.get().excecao.getMessage());
        var plano = planoRepository.buscarPorId(PLANO_ID);
        assertNotNull(plano, "Plano não encontrado");
        var procedimento = plano.getProcedimentos().get(0);
        assertEquals(
                com.g4.odontohub.tratamento.domain.StatusProcedimento.valueOf(statusEsperado),
                procedimento.getStatus()
        );
    }
}
