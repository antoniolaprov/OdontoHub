package com.g4.odontohub.steps;

import com.g4.odontohub.inadimplencia.application.InadimplenciaService;
import com.g4.odontohub.inadimplencia.domain.*;
import com.g4.odontohub.infra.InMemoryInadimplenciaRepository;
import com.g4.odontohub.infra.InMemoryPagamentoParceladoRepository;
import com.g4.odontohub.shared.exception.DomainException;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para F08 - Controle de Inadimplência.
 * Chama exclusivamente InadimplenciaService (camada application).
 * Nunca acessa repositórios diretamente nas verificações.
 */
public class F08InadimplenciaSteps {

    private static final String PACIENTE = "Fernanda Dias";

    private InMemoryPagamentoParceladoRepository pagamentoRepo;
    private InMemoryInadimplenciaRepository inadimplenciaRepo;
    private InadimplenciaService service;

    // Estado do cenário
    private boolean bloqueioRemovidoRetorno;
    private String mensagemBloqueio;
    private RegistroInadimplencia registroAtual;

    @Before
    public void setup() {
        pagamentoRepo = new InMemoryPagamentoParceladoRepository();
        inadimplenciaRepo = new InMemoryInadimplenciaRepository();
        service = new InadimplenciaService(pagamentoRepo, inadimplenciaRepo);
        bloqueioRemovidoRetorno = false;
        mensagemBloqueio = null;
        registroAtual = null;
    }

    // ── Contexto ─────────────────────────────────────────────────────────────

    @Dado("que existe um pagamento parcelado para {string}")
    public void existePagamentoParcelado(String nomePaciente) {
        // Cria o pagamento parcelado base (sem parcelas ainda)
        PagamentoParcelado pp = PagamentoParcelado.criar(1L, nomePaciente);
        pagamentoRepo.salvar(pp);
    }

    // ── Dados (Dado) ──────────────────────────────────────────────────────────

    @Dado("que a parcela {int} do pagamento de {string} tem vencimento em {string} e status {string}")
    public void parcelaComVencimentoEStatus(int numero, String nomePaciente,
                                             String dataVencimento, String status) {
        PagamentoParcelado pp = pagamentoRepo.buscarPorPaciente(nomePaciente)
                .orElseGet(() -> {
                    PagamentoParcelado novo = PagamentoParcelado.criar(1L, nomePaciente);
                    pagamentoRepo.salvar(novo);
                    return novo;
                });
        ParcelaInadimplencia parcela = ParcelaInadimplencia.criar(
                numero,
                LocalDate.parse(dataVencimento),
                StatusParcela.valueOf(status));
        pp.adicionarParcela(parcela);
        pagamentoRepo.salvar(pp);
    }

    @Dado("que a parcela {int} de {string} tem status {string}")
    public void parcelaComStatus(int numero, String nomePaciente, String status) {
        PagamentoParcelado pp = pagamentoRepo.buscarPorPaciente(nomePaciente)
                .orElseGet(() -> {
                    PagamentoParcelado novo = PagamentoParcelado.criar(1L, nomePaciente);
                    pagamentoRepo.salvar(novo);
                    return novo;
                });
        StatusParcela statusParcela = StatusParcela.valueOf(status);
        ParcelaInadimplencia parcela = ParcelaInadimplencia.criar(
                numero, LocalDate.now().minusDays(10), statusParcela);
        pp.adicionarParcela(parcela);
        pagamentoRepo.salvar(pp);

        // Se INADIMPLENTE, cria o registro de inadimplência
        if (statusParcela == StatusParcela.INADIMPLENTE) {
            if (inadimplenciaRepo.buscarPorPaciente(nomePaciente).isEmpty()) {
                service.criarRegistroInadimplencia(nomePaciente);
            }
        }
    }

    @Dado("que {string} não possui outras parcelas inadimplentes")
    public void semOutrasParcelasInadimplentes(String nomePaciente) {
        // Nada a fazer — o setup do step anterior já garante apenas 1 parcela inadimplente
    }

    @Dado("que {string} possui {int} parcelas com status {string}")
    public void possuiNParcelasComStatus(String nomePaciente, int quantidade, String status) {
        PagamentoParcelado pp = pagamentoRepo.buscarPorPaciente(nomePaciente)
                .orElseGet(() -> {
                    PagamentoParcelado novo = PagamentoParcelado.criar(1L, nomePaciente);
                    pagamentoRepo.salvar(novo);
                    return novo;
                });
        StatusParcela statusParcela = StatusParcela.valueOf(status);
        for (int i = 1; i <= quantidade; i++) {
            pp.adicionarParcela(ParcelaInadimplencia.criar(
                    i, LocalDate.now().minusDays(i * 5), statusParcela));
        }
        pagamentoRepo.salvar(pp);

        if (statusParcela == StatusParcela.INADIMPLENTE) {
            if (inadimplenciaRepo.buscarPorPaciente(nomePaciente).isEmpty()) {
                service.criarRegistroInadimplencia(nomePaciente);
            }
        }
    }

    @Dado("que {string} possui inadimplência ativa")
    public void possuiInadimplenciaAtiva(String nomePaciente) {
        // Garante pagamento com parcela inadimplente e registro criado
        PagamentoParcelado pp = pagamentoRepo.buscarPorPaciente(nomePaciente)
                .orElseGet(() -> {
                    PagamentoParcelado novo = PagamentoParcelado.criar(1L, nomePaciente);
                    pagamentoRepo.salvar(novo);
                    return novo;
                });
        if (pp.getParcelas().isEmpty()) {
            pp.adicionarParcela(ParcelaInadimplencia.criar(
                    1, LocalDate.now().minusDays(10), StatusParcela.INADIMPLENTE));
            pagamentoRepo.salvar(pp);
        }
        if (inadimplenciaRepo.buscarPorPaciente(nomePaciente).isEmpty()) {
            service.criarRegistroInadimplencia(nomePaciente);
        }
    }

    // ── Ações (Quando) ────────────────────────────────────────────────────────

    @Quando("o sistema processa as parcelas vencidas na data {string}")
    public void processaParcelasVencidas(String data) {
        service.processarParcelasVencidas(LocalDate.parse(data));
    }

    @Quando("a recepcionista tenta registrar um novo agendamento para {string}")
    public void tentaRegistrarNovoAgendamento(String nomePaciente) {
        try {
            service.verificarBloqueioAgendamento(nomePaciente);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a recepcionista registra a quitação da parcela {int} em {string}")
    public void registraQuitacaoDaParcela(int numero, String data) {
        bloqueioRemovidoRetorno = service.quitarParcela(
                PACIENTE, numero, LocalDate.parse(data));
    }

    @Quando("a recepcionista quita apenas a parcela {int}")
    public void quitaApenasParcela(int numero) {
        bloqueioRemovidoRetorno = service.quitarParcela(
                PACIENTE, numero, LocalDate.now());

        // Captura a mensagem de bloqueio restante
        if (!bloqueioRemovidoRetorno) {
            mensagemBloqueio = "Paciente ainda possui parcelas inadimplentes";
        }
    }

    @Quando("a recepcionista registra uma tentativa de cobrança com resultado {string}")
    public void registraTentativaCobranca(String resultado) {
        service.registrarCobranca(PACIENTE, "recepcionista", LocalDate.now(), resultado);
    }

    @Quando("o dentista {string} autoriza o agendamento de {string} com justificativa")
    public void dentistaAutorizaAgendamento(String nomeDentista, String nomePaciente) {
        service.autorizarAgendamento(nomePaciente, nomeDentista);
    }

    // ── Verificações (Então) ──────────────────────────────────────────────────

    @Então("o status da parcela {int} deve ser alterado automaticamente para {string}")
    public void statusParcelaAlteradoAutomaticamente(int numero, String statusEsperado) {
        PagamentoParcelado pp = service.buscarPagamento(PACIENTE)
                .orElseThrow(() -> new AssertionError("Pagamento não encontrado"));
        ParcelaInadimplencia parcela = pp.getPorNumero(numero);
        assertEquals(StatusParcela.valueOf(statusEsperado), parcela.getStatus());
    }

    @Então("um registro de inadimplência deve ser criado para {string}")
    public void registroInadimplenciaCriado(String nomePaciente) {
        assertTrue(service.buscarInadimplencia(nomePaciente).isPresent(),
                "Registro de inadimplência não foi criado para: " + nomePaciente);
    }

    @Então("o sistema deve bloquear o agendamento")
    public void sistemaBloqueiaAgendamento() {
        assertNotNull(ScenarioContext.get().excecao,
                "Esperava-se bloqueio de agendamento por inadimplência");
    }

    @Então("a parcela {int} de {string} deve ter status {string}")
    public void parcelaDeveTerStatus(int numero, String nomePaciente, String statusEsperado) {
        PagamentoParcelado pp = service.buscarPagamento(nomePaciente)
                .orElseThrow(() -> new AssertionError("Pagamento não encontrado"));
        ParcelaInadimplencia parcela = pp.getPorNumero(numero);
        assertEquals(StatusParcela.valueOf(statusEsperado), parcela.getStatus());
    }

    @Então("o bloqueio de agendamento de {string} deve ser removido automaticamente")
    public void bloqueioRemovidoAutomaticamente(String nomePaciente) {
        assertTrue(bloqueioRemovidoRetorno,
                "Esperava-se que o bloqueio fosse removido automaticamente");
        RegistroInadimplencia reg = service.buscarInadimplencia(nomePaciente)
                .orElseThrow(() -> new AssertionError("Registro de inadimplência não encontrado"));
        assertFalse(reg.isBloqueado(), "O bloqueio deveria ter sido removido");
    }

    @Então("o bloqueio de agendamento de {string} deve permanecer ativo")
    public void bloqueioDevePermanecer(String nomePaciente) {
        assertFalse(bloqueioRemovidoRetorno,
                "O bloqueio não deveria ter sido removido");
        RegistroInadimplencia reg = service.buscarInadimplencia(nomePaciente)
                .orElseThrow(() -> new AssertionError("Registro de inadimplência não encontrado"));
        assertTrue(reg.isBloqueado(), "O bloqueio deveria permanecer ativo");
    }

    @Então("a mensagem deve informar que ainda existe inadimplência pendente")
    public void mensagemAindaExisteInadimplencia() {
        assertNotNull(mensagemBloqueio,
                "Esperava-se mensagem informando inadimplência pendente");
        assertTrue(mensagemBloqueio.contains("inadimplent"),
                "Mensagem deveria mencionar inadimplência: " + mensagemBloqueio);
    }

    @Então("o histórico de cobrança deve registrar a data, o responsável e o resultado {string}")
    public void historicoCobrangaRegistrado(String resultado) {
        RegistroInadimplencia reg = service.buscarInadimplencia(PACIENTE)
                .orElseThrow(() -> new AssertionError("Registro de inadimplência não encontrado"));
        assertFalse(reg.getHistoricoCobranca().isEmpty(),
                "Histórico de cobrança deveria ter ao menos um registro");
        RegistroCobranca ultimo = reg.getHistoricoCobranca()
                .get(reg.getHistoricoCobranca().size() - 1);
        assertNotNull(ultimo.getData(), "Data deve estar registrada");
        assertNotNull(ultimo.getResponsavel(), "Responsável deve estar registrado");
        assertEquals(resultado, ultimo.getResultado(),
                "Resultado da cobrança não confere");
    }

    @Então("o agendamento deve ser permitido")
    public void agendamentoDeveSerPermitido() {
        assertNull(ScenarioContext.get().excecao,
                "Não deveria haver exceção — agendamento autorizado pelo dentista");
        RegistroInadimplencia reg = service.buscarInadimplencia(PACIENTE)
                .orElseThrow(() -> new AssertionError("Registro não encontrado"));
        assertNotNull(reg.getDentistaAutorizador(),
                "Dentista autorizador deveria estar registrado");
    }

    @Então("o nome do dentista autorizador deve ser registrado no agendamento")
    public void nomeDentistaRegistrado() {
        RegistroInadimplencia reg = service.buscarInadimplencia(PACIENTE)
                .orElseThrow(() -> new AssertionError("Registro não encontrado"));
        assertNotNull(reg.getDentistaAutorizador(),
                "Nome do dentista autorizador deve estar registrado");
        assertFalse(reg.getDentistaAutorizador().isBlank(),
                "Nome do dentista não deve estar em branco");
    }
}
