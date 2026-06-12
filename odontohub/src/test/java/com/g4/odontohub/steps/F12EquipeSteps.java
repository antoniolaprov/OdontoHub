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
        assertEquals(StatusColaborador.valueOf(status.toUpperCase()), colaborador.getStatus());
    }

    @E("a função {string} deve estar registrada")
    public void funcaoDeveEstarRegistrada(String funcao) {
        Colaborador colaborador = service.getColaborador("Juliana Mendes");
        assertNotNull(colaborador);
        assertEquals(FuncaoColaborador.valueOf(funcao.toUpperCase()), colaborador.getFuncao());
    }

    @Quando("o dentista tenta cadastrar um colaborador sem informar a função")
    public void cadastrarSemFuncao() {
        service.cadastrarSemFuncao("Colaborador Teste", "111.111.111-11", "81900000000");
    }

    @Então("o sistema deve rejeitar o cadastro de colaborador")
    public void sistemaDeveRejeitarCadastroColaborador() {
        assertTrue(service.houveErro());
    }

    @Então("o sistema deve rejeitar o cadastro")
    public void sistemaDeveRejeitarOCadastro() {
        assertTrue(service.houveErro());
    }

    @E("a mensagem de erro de colaborador deve informar {string}")
    public void mensagemDeErroDeColaboradorDeveInformar(String mensagem) {
        assertEquals(mensagem, service.getUltimoErro());
    }

    @Quando("o dentista tenta cadastrar o colaborador {string} sem informar o CPF")
    public void cadastrarSemCpf(String nome) {
        service.cadastrarSemCpf(nome);
    }

    @Dado("que o colaborador {string} está ativo no sistema")
    public void colaboradorAtivoNoSistema(String nome) {
        service.adicionarColaboradorComStatus(nome, "AUXILIAR", "Ativo");
    }

    @Dado("que o colaborador {string} está inativo no sistema")
    public void colaboradorInativoNoSistema(String nome) {
        service.adicionarColaboradorComStatus(nome, "AUXILIAR", "Inativo");
    }

    @Dado("que o colaborador {string} com função {string} está inativo no sistema")
    public void colaboradorComFuncaoInativoNoSistema(String nome, String funcao) {
        service.adicionarColaboradorComStatus(nome, funcao, "Inativo");
    }

    @Quando("o dentista desativa o colaborador {string}")
    public void desativarColaborador(String nome) {
        service.desativarColaborador(nome);
    }

    @Quando("o dentista reativa o colaborador {string}")
    public void reativarColaborador(String nome) {
        service.reativarColaborador(nome);
    }

    @Então("o status do colaborador deve ser alterado para {string}")
    public void statusDoColaboradorAlteradoPara(String status) {
        Colaborador colaborador = service.getColaborador("Juliana Mendes");
        assertNotNull(colaborador);
        assertEquals(StatusColaborador.valueOf(status.toUpperCase()), colaborador.getStatus());
    }

    @E("os dados de {string} devem permanecer no sistema")
    public void dadosDevemPermanecerNoSistema(String nome) {
        assertTrue(service.colaboradorExisteNoSistema(nome));
    }

    @Quando("o auxiliar abre a lista de responsáveis disponíveis para registro de esterilização")
    public void abrirListaResponsaveis() {
        // ação implícita
    }

    @Quando("o sistema lista os responsáveis disponíveis para esterilização")
    public void listarResponsaveisEsterilizacao() {
        // ação implícita
    }

    @Então("{string} não deve aparecer na lista de responsáveis")
    public void naoDeveAparecerNaListaDeResponsaveis(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        assertFalse(lista.stream().anyMatch(c -> c.getNome().equals(nome)));
    }

    @Dado("que {string} tem função {string} e está ativo")
    public void colaboradorComFuncaoAtivo(String nome, String funcao) {
        service.adicionarColaboradorComStatus(nome, funcao, "Ativo");
    }

    @E("que {string} tem função {string} e está ativa")
    public void colaboradorComFuncaoAtiva(String nome, String funcao) {
        service.adicionarColaboradorComStatus(nome, funcao, "Ativo");
    }

    @Então("{string} deve constar na lista de responsáveis")
    public void deveConstarNaListaDeResponsaveis(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        assertTrue(lista.stream().anyMatch(c -> c.getNome().equals(nome)));
    }

    @E("{string} não deve constar na lista de responsáveis")
    public void naoDeveConstarNaListaDeResponsaveis(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        assertFalse(lista.stream().anyMatch(c -> c.getNome().equals(nome)));
    }

    @Dado("que o colaborador {string} está com status {string}")
    public void queOColaboradorEstaComStatus(String nome, String status) {
        service.adicionarColaboradorComStatus(nome, "AUXILIAR", status);
    }

    @Dado("que o colaborador {string} com função {string} está com status {string}")
    public void queOColaboradorComFuncaoEstaComStatus(String nome, String funcao, String status) {
        service.adicionarColaboradorComStatus(nome, funcao, status);
    }

    @Dado("que {string} tem função {string} e status {string}")
    public void queTemFuncaoEStatus(String nome, String funcao, String status) {
        service.adicionarColaboradorComStatus(nome, funcao, status);
    }

    @Então("o status deve ser alterado para {string}")
    public void oStatusDeveSerAlteradoPara(String status) {
        Colaborador colaborador = service.getColaborador("Juliana Mendes");
        assertNotNull(colaborador);
        assertEquals(StatusColaborador.valueOf(status.toUpperCase()), colaborador.getStatus());
    }

    @Então("{string} não deve aparecer na lista")
    public void naoDeveAparecerNaLista(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        assertFalse(lista.stream().anyMatch(c -> c.getNome().equals(nome)),
                "Esperava que " + nome + " não aparecesse na lista");
    }

    @Então("{string} deve constar na lista")
    public void deveConstarNaLista(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        assertTrue(lista.stream().anyMatch(c -> c.getNome().equals(nome)),
                "Esperava que " + nome + " constasse na lista de responsáveis");
    }

    @Então("{string} não deve constar na lista")
    public void naoDeveConstarNaLista(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        assertFalse(lista.stream().anyMatch(c -> c.getNome().equals(nome)),
                "Esperava que " + nome + " não constasse na lista de responsáveis");
    }

    @E("{string} deve voltar a aparecer nas listas de seleção")
    public void deveVoltarAAparecer(String nome) {
        List<Colaborador> lista = service.listarResponsaveisEsterilizacao();
        assertTrue(lista.stream().anyMatch(c -> c.getNome().equals(nome)));
    }
}