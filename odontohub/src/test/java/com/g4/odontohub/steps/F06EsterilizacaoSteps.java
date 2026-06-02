package com.g4.odontohub.steps;

import com.g4.odontohub.estoque.application.InstrumentoApplicationService;
import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.model.StatusEsterilizacao;
import com.g4.odontohub.estoque.domain.model.StatusInstrumento;
import io.cucumber.java.pt.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class F06EsterilizacaoSteps {

    private final InstrumentoApplicationService service = new InstrumentoApplicationService();
    private Instrumento ultimoInstrumento;
    private List<Instrumento> listaResultado;
    private Exception ultimaExcecao;
    private String nomeInstrumentoAtual;

    // ─────────────────────── BACKGROUND ───────────────────────

    @Dado("que o prazo global de validade da esterilização é de {int} dias")
    public void prazoGlobalValidadeEsterilizacao(int prazo) {
        service.recalcularValidadeGlobal(prazo);
    }

    @Dado("que o instrumento {string} está cadastrado com prazo de validade de {int} dias")
    public void instrumentoCadastrado(String nome, int prazo) {
        service.cadastrar(nome, prazo);
    }

    @Dado("que o instrumento {string} está cadastrado")
    public void instrumentoCadastrado(String nome) {
        cadastrarInstrumentoParaCenario(nome);
    }

    // ──────────── CENÁRIO 1: Cadastro de instrumento ──────────

    @Quando("o auxiliar cadastra o instrumento {string} da categoria {string} com código {string}")
    public void cadastrarInstrumento(String nome, String categoria, String codigoIdentificador) {
        ultimoInstrumento = service.cadastrarInstrumento(nome, categoria, codigoIdentificador);
    }

    @Então("o instrumento deve ser salvo no sistema")
    public void instrumentoDeveSerSalvo() {
        assertNotNull(ultimoInstrumento);
        assertSame(ultimoInstrumento, service.buscarPorNome(ultimoInstrumento.getNome()));
    }

    @E("o status inicial deve ser {string}")
    public void statusInicialDeveSer(String statusStr) {
        assertEquals(mapearStatus(statusStr), ultimoInstrumento.getStatus());
    }

    @E("o instrumento deve estar marcado como ativo")
    public void instrumentoDeveEstarAtivo() {
        assertEquals(StatusInstrumento.ATIVO, ultimoInstrumento.getStatusInstrumento());
    }

    // ──────────── CENÁRIO 2: Bloqueio de código duplicado ─────

    @Dado("que já existe um instrumento cadastrado com código {string}")
    public void instrumentoCadastradoComCodigo(String codigoIdentificador) {
        service.cadastrarInstrumento("Instrumento existente", "", codigoIdentificador);
    }

    @Quando("o auxiliar tenta cadastrar outro instrumento com código {string}")
    public void tentarCadastrarInstrumentoComCodigo(String codigoIdentificador) {
        try {
            service.cadastrarInstrumento("Outro instrumento", "", codigoIdentificador);
        } catch (Exception e) {
            ultimaExcecao = e;
        }
    }

    @Então("o sistema deve impedir o cadastro")
    public void sistemaDeveImpedirCadastro() {
        assertNotNull(ultimaExcecao);
    }

    @E("deve exibir a mensagem {string}")
    public void deveExibirMensagem(String mensagem) {
        assertEquals(mensagem, ultimaExcecao.getMessage());
    }

    // ──────────── CENÁRIO 3: Desativação de instrumento ───────

    @Dado("que o instrumento {string} está ativo")
    public void instrumentoEstaAtivo(String nome) {
        ultimoInstrumento = cadastrarInstrumentoParaCenario(nome);
        nomeInstrumentoAtual = nome;
    }

    @Quando("o auxiliar desativa o instrumento")
    public void desativarInstrumento() {
        service.desativarInstrumento(nomeInstrumentoAtual);
    }

    @Então("o instrumento não deve aparecer em novas listas de esterilização")
    public void instrumentoNaoDeveAparecerEmListasDeEsterilizacao() {
        listaResultado = service.listarInstrumentosAtivos();
        assertFalse(listaResultado.stream().anyMatch(i -> i.getNome().equals(nomeInstrumentoAtual)));
    }

    @E("o instrumento deve permanecer salvo para fins históricos")
    public void instrumentoDevePermanecerSalvo() {
        Instrumento instrumento = service.buscarPorNome(nomeInstrumentoAtual);
        assertEquals(StatusInstrumento.INATIVO, instrumento.getStatusInstrumento());
    }

    // ──────────── CENÁRIO 4: Marcação como Estéril ────────────

    @Quando("o auxiliar {string} marca {string} como Estéril na data de hoje")
    public void marcarComoEsteril(String responsavel, String nome) {
        service.marcarComoEsteril(nome, LocalDate.now(), responsavel);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    @Então("o status do instrumento deve ser {string}")
    public void statusInstrumentoDeveSer(String statusStr) {
        assertNotNull(ultimoInstrumento);
        assertEquals(mapearStatus(statusStr), ultimoInstrumento.getStatus());
    }

    @E("a data da última esterilização deve ser registrada como hoje")
    public void dataEsterilizacaoDeveSerHoje() {
        assertEquals(LocalDate.now(), ultimoInstrumento.getDataUltimaEsterilizacao());
    }

    @E("o responsável deve ser registrado como {string}")
    public void responsavelDeveSerRegistrado(String responsavel) {
        assertEquals(responsavel, ultimoInstrumento.getResponsavelEsterilizacao());
    }

    @E("a data de vencimento deve ser calculada como hoje mais {int} dias")
    public void dataVencimentoDeveSerHojeMaisDias(int dias) {
        assertEquals(LocalDate.now().plusDays(dias), ultimoInstrumento.getDataVencimento());
    }

    // ──────────── CENÁRIO 5: Vencimento automático ────────────

    @Dado("que {string} foi esterilizado há {int} dias com status {string}")
    public void instrumentoEsterilizadoHaDias(String nome, int diasAtras, String statusStr) {
        try {
            service.buscarPorNome(nome);
        } catch (Exception e) {
            service.cadastrar(nome, 7);
        }

        service.marcarComoEsteril(nome, LocalDate.now().minusDays(diasAtras), "Sistema");

        StatusEsterilizacao status = mapearStatus(statusStr);
        service.definirStatus(nome, status, null);
    }

    @Quando("o sistema verifica a validade dos instrumentos")
    public void sistemaVerificaValidade() {
        service.verificarEAtualizarVencidos();
    }

    @Então("o status de {string} deve ser atualizado para {string}")
    public void statusDeveSerAtualizado(String nome, String statusStr) {
        assertEquals(mapearStatus(statusStr), service.buscarPorNome(nome).getStatus());
    }

    // ──────────── CENÁRIO 6: Recálculo de validade global ─────

    @Dado("que {string} foi esterilizado hoje com prazo global de {int} dias")
    public void instrumentoEsterilizadoHojeComPrazo(String nome, int prazo) {
        try {
            service.buscarPorNome(nome);
        } catch (Exception e) {
            service.cadastrar(nome, prazo);
        }

        service.marcarComoEsteril(nome, LocalDate.now(), "Sistema");
    }

    @Quando("o dentista altera o prazo global de esterilização para {int} dias")
    public void alterarPrazoGlobal(int novoPrazo) {
        service.recalcularValidadeGlobal(novoPrazo);
    }

    @Então("a nova data de vencimento de {string} deve ser calculada como hoje mais {int} dias")
    public void novaDataVencimentoDeveSer(String nome, int dias) {
        assertEquals(LocalDate.now().plusDays(dias), service.buscarPorNome(nome).getDataVencimento());
    }

    // ──────────── CENÁRIO 7: Listagem de instrumentos prontos ─

    @Dado("que {string} está com status {string} e dentro do prazo")
    public void instrumentoComStatusEDentroDoPrazo(String nome, String statusStr) {
        try {
            service.buscarPorNome(nome);
        } catch (Exception e) {
            service.cadastrar(nome, 7);
        }

        service.marcarComoEsteril(nome, LocalDate.now(), "Sistema");
    }

    @Dado("que {string} está com status {string}")
    public void instrumentoComStatus(String nome, String statusStr) {
        try {
            service.buscarPorNome(nome);
        } catch (Exception e) {
            service.cadastrar(nome, 7);
        }

        StatusEsterilizacao status = mapearStatus(statusStr);
        LocalDate dataVenc = status == StatusEsterilizacao.VENCIDO ? LocalDate.now().minusDays(1) : null;

        service.definirStatus(nome, status, dataVenc);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    @Quando("o auxiliar solicita a lista de instrumentos prontos para uso")
    public void solicitarListaProntosParaUso() {
        listaResultado = service.listarEstereisDentroDoPrazo();
    }

    @Então("a lista deve conter apenas {string}")
    public void listaDeveConterApenas(String nome) {
        assertEquals(1, listaResultado.size());
        assertEquals(nome, listaResultado.get(0).getNome());
    }

    @E("a lista não deve conter {string}")
    public void listaNaoDeveConter(String nome) {
        boolean encontrado = listaResultado.stream().anyMatch(i -> i.getNome().equals(nome));
        assertFalse(encontrado, "A lista não deveria conter: " + nome);
    }

    // ──────────── CENÁRIO 8: Marcação como Contaminado ────────

    @Quando("o auxiliar marca {string} como Contaminado após uso no procedimento")
    public void marcarComoContaminado(String nome) {
        service.marcarComoContaminado(nome);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    // ─────────────────────── HELPERS ──────────────────────────

    private Instrumento cadastrarInstrumentoParaCenario(String nome) {
        try {
            return service.buscarPorNome(nome);
        } catch (Exception e) {
            return service.cadastrar(nome, 7);
        }
    }

    private StatusEsterilizacao mapearStatus(String statusStr) {
        return switch (statusStr) {
            case "Estéril"     -> StatusEsterilizacao.ESTERIL;
            case "Vencido"     -> StatusEsterilizacao.VENCIDO;
            case "Contaminado" -> StatusEsterilizacao.CONTAMINADO;
            default -> throw new IllegalArgumentException("Status desconhecido: " + statusStr);
        };
    }
}
