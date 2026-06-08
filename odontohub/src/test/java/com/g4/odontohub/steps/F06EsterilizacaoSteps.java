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
    private static Instrumento ultimoInstrumentoCompartilhado;

    public static void definirUltimoInstrumentoCompartilhado(Instrumento instrumento) {
        ultimoInstrumentoCompartilhado = instrumento;
    }

    @Dado("que o instrumento {string} está cadastrado na F16 com prazo de validade de {int} dias e status ativo")
    public void instrumentoCadastradoNaF16ComPrazoEAtivo(String nome, int prazo) {
        ultimoInstrumento = cadastrarSeNecessario(nome, prazo);
    }

    @Dado("que o instrumento {string} está cadastrado na F16 com status ativo e status de esterilização {string}")
    public void instrumentoCadastradoNaF16ComStatusEsterilizacao(String nome, String statusStr) {
        ultimoInstrumento = cadastrarSeNecessario(nome, 7);
        definirStatus(nome, mapearStatus(statusStr));
    }

    @Dado("que o instrumento {string} está cadastrado na F16 com status inativo")
    public void instrumentoCadastradoNaF16ComStatusInativo(String nome) {
        ultimoInstrumento = cadastrarSeNecessario(nome, 7);
        service.desativarInstrumento(nome);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    @Dado("que {string} está com status {string}")
    public void instrumentoComStatus(String nome, String statusStr) {
        ultimoInstrumento = cadastrarSeNecessario(nome, 7);
        definirStatus(nome, mapearStatus(statusStr));
    }

    @Dado("que {string} está com status {string} e dentro do prazo")
    public void instrumentoComStatusEDentroDoPrazo(String nome, String statusStr) {
        ultimoInstrumento = cadastrarSeNecessario(nome, 7);
        StatusEsterilizacao status = mapearStatus(statusStr);
        if (status == StatusEsterilizacao.ESTERIL) {
            service.marcarComoEsteril(nome, LocalDate.now(), "Sistema");
        } else {
            definirStatus(nome, status);
        }
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    @Dado("que {string} foi esterilizado há {int} dias com status {string}")
    public void instrumentoEsterilizadoHaDias(String nome, int diasAtras, String statusStr) {
        ultimoInstrumento = cadastrarSeNecessario(nome, 7);
        service.marcarComoEsteril(nome, LocalDate.now().minusDays(diasAtras), "Sistema");
        definirStatus(nome, mapearStatus(statusStr));
    }

    @Quando("o auxiliar {string} marca {string} como Estéril na data de hoje")
    public void marcarComoEsteril(String responsavel, String nome) {
        service.marcarComoEsteril(nome, LocalDate.now(), responsavel);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    @Quando("o sistema verifica a validade dos instrumentos")
    public void sistemaVerificaValidade() {
        service.verificarEAtualizarVencidos();
    }

    @Quando("o auxiliar solicita a lista de instrumentos prontos para uso")
    public void solicitarListaProntosParaUso() {
        listaResultado = service.listarEstereisDentroDoPrazo();
    }

    @Quando("o auxiliar marca {string} como Contaminado após uso no procedimento")
    public void marcarComoContaminado(String nome) {
        service.marcarComoContaminado(nome);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    @Então("o status do instrumento deve ser {string}")
    public void statusInstrumentoDeveSer(String statusStr) {
        Instrumento instrumento = ultimoInstrumento != null ? ultimoInstrumento : ultimoInstrumentoCompartilhado;
        assertNotNull(instrumento);
        if (ehStatusInstrumento(statusStr)) {
            assertEquals(StatusInstrumento.valueOf(statusStr), instrumento.getStatusInstrumento());
        } else {
            assertEquals(mapearStatus(statusStr), instrumento.getStatus());
        }
    }

    @Então("o status de {string} deve ser atualizado para {string}")
    public void statusDeveSerAtualizado(String nome, String statusStr) {
        assertEquals(mapearStatus(statusStr), service.buscarPorNome(nome).getStatus());
    }

    @Então("a lista deve conter {string}")
    public void listaDeveConter(String nome) {
        assertNotNull(listaResultado);
        assertTrue(listaResultado.stream().anyMatch(i -> i.getNome().equals(nome)),
                "A lista deveria conter: " + nome);
    }

    @E("a lista não deve conter {string}")
    public void listaNaoDeveConter(String nome) {
        assertNotNull(listaResultado);
        assertFalse(listaResultado.stream().anyMatch(i -> i.getNome().equals(nome)),
                "A lista não deveria conter: " + nome);
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

    private Instrumento cadastrarSeNecessario(String nome, int prazo) {
        try {
            return service.buscarPorNome(nome);
        } catch (Exception e) {
            return service.cadastrar(nome, prazo);
        }
    }

    private void definirStatus(String nome, StatusEsterilizacao status) {
        LocalDate dataVencimento = status == StatusEsterilizacao.VENCIDO ? LocalDate.now().minusDays(1) : null;
        service.definirStatus(nome, status, dataVencimento);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    private StatusEsterilizacao mapearStatus(String statusStr) {
        return StatusEsterilizacao.valueOf(statusStr);
    }

    private boolean ehStatusInstrumento(String statusStr) {
        try {
            StatusInstrumento.valueOf(statusStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
