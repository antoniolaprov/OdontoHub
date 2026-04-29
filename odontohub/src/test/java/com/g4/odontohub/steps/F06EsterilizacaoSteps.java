package com.g4.odontohub.steps;

import com.g4.odontohub.estoque.application.InstrumentoApplicationService;
import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.model.StatusEsterilizacao;
import com.g4.odontohub.relacionamentopaciente.domain.model.StatusChurn;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class F06EsterilizacaoSteps {

    private final InstrumentoApplicationService service = new InstrumentoApplicationService();
    private Instrumento ultimoInstrumento;
    private List<Instrumento> listaResultado;

    @Dado("que o instrumento {string} está cadastrado com prazo de validade de {int} dias")
    public void instrumentoCadastrado(String nome, int prazo) {
        service.cadastrar(nome, prazo);
    }

    @Quando("o auxiliar {string} marca {string} como Estéril na data de hoje")
    public void marcarComoEsteril(String responsavel, String nome) {
        service.marcarComoEsteril(nome, LocalDate.now(), responsavel);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    @Entao("o status do instrumento deve ser {string}")
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

    @Dado("que {string} foi esterilizado há {int} dias com status {string}")
    public void instrumentoEsterilizadoHaDias(String nome, int diasAtras, String statusStr) {
        try {
            service.buscarPorNome(nome);
        } catch (Exception e) {
            service.cadastrar(nome, 7);
        }

        service.marcarComoEsteril(nome, LocalDate.now().minusDays(diasAtras), "Sistema");
        service.definirStatus(nome, mapearStatus(statusStr), null);
    }

    @Quando("o sistema verifica a validade dos instrumentos")
    public void sistemaVerificaValidade() {
        service.verificarEAtualizarVencidos();
    }

    @Entao("o status de {string} deve ser atualizado para {string}")
    public void statusDeveSerAtualizado(String nome, String statusStr) {
        try {
            assertEquals(mapearStatus(statusStr), service.buscarPorNome(nome).getStatus());
        } catch (IllegalArgumentException ex) {
            assertEquals(mapearStatusChurn(statusStr),
                    SharedTestServices.getChurnApplicationService().buscarAnalisePorPaciente(nome).getStatusChurn());
        }
    }

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

    @Entao("a nova data de vencimento de {string} deve ser calculada como hoje mais {int} dias")
    public void novaDataVencimentoDeveSer(String nome, int dias) {
        assertEquals(LocalDate.now().plusDays(dias), service.buscarPorNome(nome).getDataVencimento());
    }

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

    @Entao("a lista deve conter apenas {string}")
    public void listaDeveConterApenas(String nome) {
        assertEquals(1, listaResultado.size());
        assertEquals(nome, listaResultado.get(0).getNome());
    }

    @E("a lista não deve conter {string}")
    public void listaNaoDeveConter(String nome) {
        boolean encontrado = listaResultado.stream().anyMatch(i -> i.getNome().equals(nome));
        assertFalse(encontrado, "A lista não deveria conter: " + nome);
    }

    @Quando("o auxiliar marca {string} como Contaminado após uso no procedimento")
    public void marcarComoContaminado(String nome) {
        service.marcarComoContaminado(nome);
        ultimoInstrumento = service.buscarPorNome(nome);
    }

    private StatusEsterilizacao mapearStatus(String statusStr) {
        return switch (statusStr) {
            case "Estéril" -> StatusEsterilizacao.ESTERIL;
            case "Vencido" -> StatusEsterilizacao.VENCIDO;
            case "Contaminado" -> StatusEsterilizacao.CONTAMINADO;
            default -> throw new IllegalArgumentException("Status desconhecido: " + statusStr);
        };
    }

    private StatusChurn mapearStatusChurn(String statusStr) {
        return switch (statusStr) {
            case "Ativo" -> StatusChurn.ATIVO;
            case "Zona de Risco" -> StatusChurn.ZONA_DE_RISCO;
            case "Evadido" -> StatusChurn.EVADIDO;
            case "Reativado" -> StatusChurn.REATIVADO;
            default -> throw new IllegalArgumentException("Status de churn desconhecido: " + statusStr);
        };
    }
}
