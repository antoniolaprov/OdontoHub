package com.g4.odontohub.steps;

import com.g4.odontohub.equipe.application.EquipeApplicationService;
import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.model.FuncaoColaborador;
import com.g4.odontohub.equipe.domain.model.StatusColaborador;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class F12EquipeSteps {

    private final EquipeApplicationService service = new EquipeApplicationService();

    @Quando("o dentista cadastra o colaborador {string} com CPF {string}, telefone {string} e função {string}")
    public void cadastrarColaborador(String nome, String cpf, String telefone, String funcao) {
        service.cadastrarColaborador(nome, cpf, telefone, funcao);
    }

    @Então("o colaborador deve ser salvo com status {string}")
    public void colaboradorSalvoComStatus(String status) {
        Colaborador colaborador = service.getColaborador("Juliana Mendes");
        assertNotNull(colaborador);
        StatusColaborador esperado = StatusColaborador.valueOf(status.toUpperCase());
        assertEquals(esperado, colaborador.getStatus());
    }

    @E("a função {string} deve estar registrada")
    public void funcaoDeveEstarRegistrada(String funcao) {
        Colaborador colaborador = service.getColaborador("Juliana Mendes");
        assertNotNull(colaborador);
        FuncaoColaborador esperada = FuncaoColaborador.valueOf(funcao.toUpperCase());
        assertEquals(esperada, colaborador.getFuncao());
    }

    @Quando("o dentista tenta cadastrar um colaborador sem informar a função")
    public void cadastrarSemFuncao() {
        service.cadastrarSemFuncao("Colaborador Teste", "111.111.111-11", "81900000000");
    }

    @Então("o sistema deve rejeitar o cadastro")
    public void sistemaDeveRejeitarCadastro() {
        assertTrue(service.houveErro());
    }

    @E("a mensagem de erro deve informar {string}")
    public void mensagemDeErroDeveInformar(String mensagem) {
        assertEquals(mensagem, service.getUltimoErro());
    }

    @Quando("o dentista tenta cadastrar o colaborador {string} sem informar o CPF")
    public void cadastrarSemCpf(String nome) {
        service.cadastrarSemCpf(nome);
    }

    @Dado("que o colaborador {string} está com status {string}")
    public void colaboradorComStatus(String nome, String status) {
        service.adicionarColaboradorComStatus(nome, "AUXILIAR", status);
    }

    @Quando("o dentista desativa o colaborador {string}")
    public void desativarColaborador(String nome) {
        service.desativarColaborador(nome);
    }

    @Então("o status deve ser alterado para {string}")
    public void statusAlteradoPara(String status) {
        Colaborador colaborador = service.getColaborador("Juliana Mendes");
        assertNotNull(colaborador);
        StatusColaborador esperado = StatusColaborador.valueOf(status.toUpperCase());
        assertEquals(esperado, colaborador.getStatus());
    }

    @E("os dados de {string} devem permanecer no sistema")
    public void dadosDevemPermanecerNoSistema(String nome) {
        assertTrue(service.colaboradorExisteNoSistema(nome));
    }

    @Dado("que o colaborador {string} com função {string} está com status {string}")
    public void colaboradorComFuncaoEStatus(String nome, String funcao, String status) {
        service.adicionarColaboradorComStatus(nome, funcao, status);
    }

    @Quando("o auxiliar abre a lista de responsáveis disponíveis para registro de esterilização")
    public void abrirListaResponsaveis() {
        // ação implícita — a listagem é verificada no Então
    }

    @Então("{string} não deve aparecer na lista")
    public void naoDeveAparecerNaLista(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        boolean presente = lista.stream().anyMatch(c -> c.getNome().equals(nome));
        assertFalse(presente);
    }

    @Dado("que {string} tem função {string} e status {string}")
    public void colaboradorComFuncaoStatus(String nome, String funcao, String status) {
        service.adicionarColaboradorComStatus(nome, funcao, status);
    }

    @Quando("o sistema lista os responsáveis disponíveis para esterilização")
    public void listarResponsaveisEsterilizacao() {
        // ação implícita — a listagem é verificada no Então
    }

    @Então("{string} deve constar na lista")
    public void deveConstarNaLista(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        boolean presente = lista.stream().anyMatch(c -> c.getNome().equals(nome));
        assertTrue(presente);
    }

    @E("{string} não deve constar na lista")
    public void naoDeveConstarNaLista(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        boolean presente = lista.stream().anyMatch(c -> c.getNome().equals(nome));
        assertFalse(presente);
    }

    @Quando("o dentista reativa o colaborador {string}")
    public void reativarColaborador(String nome) {
        service.reativarColaborador(nome);
    }

    @E("{string} deve voltar a aparecer nas listas de seleção")
    public void deveVoltarAAparecer(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        boolean presente = lista.stream().anyMatch(c -> c.getNome().equals(nome));
        assertTrue(presente);
    }
}