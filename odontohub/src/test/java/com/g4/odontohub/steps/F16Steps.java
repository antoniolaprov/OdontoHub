package com.g4.odontohub.steps;

import com.g4.odontohub.estoque.application.InstrumentoApplicationService;
import com.g4.odontohub.estoque.domain.model.Instrumento;
import io.cucumber.java.pt.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class F16Steps {

    private final InstrumentoApplicationService service = new InstrumentoApplicationService();
    private Instrumento ultimoInstrumento;
    private Exception excecaoCapturada;

    @Quando("o auxiliar cadastra o instrumento {string} da categoria {string} com código {string} e prazo de validade de {int} dias")
    public void cadastrarInstrumentoComPrazo(String nome, String categoria, String codigo, int prazo) {
        ultimoInstrumento = service.cadastrarInstrumentoComPrazo(nome, categoria, codigo, prazo);
        F06EsterilizacaoSteps.definirUltimoInstrumentoCompartilhado(ultimoInstrumento);
    }

    @Quando("o auxiliar tenta cadastrar um instrumento sem informar o nome")
    public void tentarCadastrarSemNome() {
        tentarExecutar(() -> ultimoInstrumento = service.cadastrarInstrumentoComPrazo(
                "", "Pinças", "PC-A01", 30));
    }

    @Quando("o auxiliar tenta cadastrar um instrumento sem informar a categoria")
    public void tentarCadastrarSemCategoria() {
        tentarExecutar(() -> ultimoInstrumento = service.cadastrarInstrumentoComPrazo(
                "Pinça Clínica Nº1", "", "PC-A01", 30));
    }

    @Quando("o auxiliar tenta cadastrar um instrumento sem informar o código")
    public void tentarCadastrarSemCodigo() {
        tentarExecutar(() -> ultimoInstrumento = service.cadastrarInstrumentoComPrazo(
                "Pinça Clínica Nº1", "Pinças", "", 30));
    }

    @Dado("que já existe um instrumento cadastrado com código {string}")
    public void existeInstrumentoComCodigo(String codigo) {
        ultimoInstrumento = service.cadastrarInstrumentoComPrazo(
                "Instrumento " + codigo, "Categoria", codigo, 30);
        F06EsterilizacaoSteps.definirUltimoInstrumentoCompartilhado(ultimoInstrumento);
    }

    @Quando("o auxiliar tenta cadastrar outro instrumento com código {string}")
    public void tentarCadastrarCodigoDuplicado(String codigo) {
        tentarExecutar(() -> ultimoInstrumento = service.cadastrarInstrumentoComPrazo(
                "Outro Instrumento", "Categoria", codigo, 30));
    }

    @Dado("que existe um instrumento cadastrado com nome {string} e status {string}")
    public void existeInstrumentoComNomeEStatus(String nome, String status) {
        ultimoInstrumento = service.cadastrarInstrumentoComPrazo(nome, "Categoria", codigoPara(nome), 30);
        F06EsterilizacaoSteps.definirUltimoInstrumentoCompartilhado(ultimoInstrumento);
    }

    @Quando("o auxiliar desativa o instrumento {string}")
    public void desativarInstrumento(String nome) {
        service.desativarInstrumento(nome);
        ultimoInstrumento = service.buscarPorNome(nome);
        F06EsterilizacaoSteps.definirUltimoInstrumentoCompartilhado(ultimoInstrumento);
    }

    @Dado("que existe um instrumento cadastrado com nome {string} e prazo de validade de {int} dias")
    public void existeInstrumentoComNomeEPrazo(String nome, int prazo) {
        ultimoInstrumento = service.cadastrarInstrumentoComPrazo(nome, "Categoria", codigoPara(nome), prazo);
        F06EsterilizacaoSteps.definirUltimoInstrumentoCompartilhado(ultimoInstrumento);
    }

    @Dado("que existe um instrumento cadastrado com nome {string} da categoria {string} e prazo de validade de {int} dias")
    public void existeInstrumentoComCategoriaEPrazo(String nome, String categoria, int prazo) {
        ultimoInstrumento = service.cadastrarInstrumentoComPrazo(nome, categoria, codigoPara(nome), prazo);
        F06EsterilizacaoSteps.definirUltimoInstrumentoCompartilhado(ultimoInstrumento);
    }

    @Quando("o auxiliar altera o prazo global de validade para {int} dias")
    public void alterarPrazoGlobal(int prazo) {
        service.configurarPrazoGlobal(prazo);
    }

    @Quando("o auxiliar altera o prazo da categoria {string} para {int} dias")
    public void alterarPrazoCategoria(String categoria, int prazo) {
        service.configurarPrazoPorCategoria(categoria, prazo);
    }

    @Quando("o auxiliar tenta configurar o prazo global de validade para {int} dias")
    public void tentarConfigurarPrazoGlobal(int prazo) {
        tentarExecutar(() -> service.configurarPrazoGlobal(prazo));
    }

    @Dado("que existe um instrumento individual cadastrado com código {string}")
    public void existeInstrumentoIndividualComCodigo(String codigo) {
        ultimoInstrumento = service.cadastrarInstrumentoComPrazo(
                "Instrumento " + codigo, "Individual", codigo, 30);
        F06EsterilizacaoSteps.definirUltimoInstrumentoCompartilhado(ultimoInstrumento);
    }

    @Quando("o auxiliar cadastra o kit {string} da categoria {string} com código {string} composto pelos instrumentos {string} e {string}")
    public void cadastrarKit(String nome, String categoria, String codigo, String codigo1, String codigo2) {
        ultimoInstrumento = service.cadastrarKit(nome, categoria, codigo, 30, List.of(codigo1, codigo2));
        F06EsterilizacaoSteps.definirUltimoInstrumentoCompartilhado(ultimoInstrumento);
    }

    @Então("o instrumento deve ser salvo no sistema")
    public void instrumentoDeveSerSalvo() {
        assertNotNull(ultimoInstrumento);
        assertSame(ultimoInstrumento, service.buscarPorNome(ultimoInstrumento.getNome()));
    }

    @Então("o tipo do instrumento deve ser {string}")
    public void tipoInstrumentoDeveSer(String tipo) {
        assertNotNull(ultimoInstrumento);
        assertEquals(tipo, ultimoInstrumento.getTipo());
    }

    @Então("o sistema deve rejeitar o cadastro do instrumento")
    public void sistemaDeveRejeitarCadastroInstrumento() {
        assertNotNull(excecaoCapturada, "O sistema deveria rejeitar o cadastro do instrumento");
    }

    @Então("a mensagem de erro do instrumento deve informar {string}")
    public void mensagemErroInstrumentoDeveInformar(String mensagem) {
        assertNotNull(excecaoCapturada, "Esperava-se uma exceção, mas nenhuma foi lançada");
        assertEquals(mensagem, excecaoCapturada.getMessage());
    }

    @Então("o sistema deve rejeitar a configuração do prazo")
    public void sistemaDeveRejeitarConfiguracaoPrazo() {
        assertNotNull(excecaoCapturada, "O sistema deveria rejeitar a configuração do prazo");
    }

    @Então("o prazo de validade de {string} deve ser {int} dias")
    public void prazoValidadeDeveSer(String nome, int prazo) {
        assertEquals(prazo, service.buscarPorNome(nome).getPrazoValidadeDias());
    }

    @Então("o kit deve conter os instrumentos {string} e {string}")
    public void kitDeveConterInstrumentos(String codigo1, String codigo2) {
        assertTrue(ultimoInstrumento.getCodigosComponentes().contains(codigo1));
        assertTrue(ultimoInstrumento.getCodigosComponentes().contains(codigo2));
    }

    private void tentarExecutar(Operacao operacao) {
        try {
            excecaoCapturada = null;
            operacao.executar();
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    private String codigoPara(String nome) {
        return nome.toUpperCase()
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    @FunctionalInterface
    private interface Operacao {
        void executar();
    }
}
