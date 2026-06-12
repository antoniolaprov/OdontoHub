package com.g4.odontohub.steps;

import com.g4.odontohub.equipe.application.ColaboradorApplicationService;
import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.model.StatusColaborador;
import io.cucumber.java.pt.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class F12EquipeSteps {

    private final ColaboradorApplicationService service = new ColaboradorApplicationService();
    private Colaborador ultimoColaborador;
    private List<Colaborador> listaResponsaveis;

    @Quando("o dentista cadastra o colaborador {string} com CPF {string}, telefone {string} e função {string}")
    public void cadastrarColaborador(String nome, String cpf, String telefone, String funcao) {
        ultimoColaborador = service.cadastrar(nome, cpf, telefone, funcao);
    }

    @Quando("o dentista tenta cadastrar um colaborador sem informar a função")
    public void tentarCadastrarSemFuncao() {
        tentar(() -> service.cadastrar("Colaborador Teste", "123.456.789-00", "81999990000", ""));
    }

    @Quando("o dentista tenta cadastrar o colaborador {string} sem informar o CPF")
    public void tentarCadastrarSemCpf(String nome) {
        tentar(() -> service.cadastrar(nome, "", "81999990000", "Auxiliar"));
    }

    @Dado("que o colaborador {string} está com status {string}")
    public void colaboradorComStatus(String nome, String status) {
        garantirCadastrado(nome, "Auxiliar");
        aplicarStatus(nome, status);
    }

    @Dado("que o colaborador {string} com função {string} está com status {string}")
    public void colaboradorComFuncaoEStatus(String nome, String funcao, String status) {
        garantirCadastrado(nome, funcao);
        aplicarStatus(nome, status);
    }

    @Dado("que {string} tem função {string} e status {string}")
    public void colaboradorTemFuncaoEStatus(String nome, String funcao, String status) {
        garantirCadastrado(nome, funcao);
        aplicarStatus(nome, status);
    }

    @Quando("o dentista desativa o colaborador {string}")
    public void desativarColaborador(String nome) {
        service.desativar(nome);
        ultimoColaborador = service.buscarPorNome(nome);
    }

    @Quando("o dentista reativa o colaborador {string}")
    public void reativarColaborador(String nome) {
        service.reativar(nome);
        ultimoColaborador = service.buscarPorNome(nome);
    }

    @Quando("o auxiliar abre a lista de responsáveis disponíveis para registro de esterilização")
    public void abrirListaResponsaveis() {
        listaResponsaveis = service.listarResponsaveisEsterilizacao();
    }

    @Quando("o sistema lista os responsáveis disponíveis para esterilização")
    public void sistemaListaResponsaveis() {
        listaResponsaveis = service.listarResponsaveisEsterilizacao();
    }

    @Então("o colaborador deve ser salvo com status {string}")
    public void colaboradorSalvoComStatus(String status) {
        assertNotNull(ultimoColaborador);
        assertEquals(StatusColaborador.valueOf(status.toUpperCase()), ultimoColaborador.getStatus());
    }

    @Então("a função {string} deve estar registrada")
    public void funcaoDeveEstarRegistrada(String funcao) {
        assertEquals(funcao.toUpperCase(), ultimoColaborador.getFuncao().name());
    }

    @Então("o status deve ser alterado para {string}")
    public void statusDeveSerAlteradoPara(String status) {
        assertEquals(StatusColaborador.valueOf(status.toUpperCase()), ultimoColaborador.getStatus());
    }

    @Então("os dados de {string} devem permanecer no sistema")
    public void dadosDevemPermanecer(String nome) {
        assertTrue(service.existe(nome), "Os dados do colaborador deveriam permanecer no sistema");
    }

    @Então("{string} não deve aparecer na lista")
    public void naoDeveAparecerNaLista(String nome) {
        assertNotNull(listaResponsaveis);
        assertFalse(listaResponsaveis.stream().anyMatch(c -> c.getNomeCompleto().equals(nome)),
                "A lista não deveria conter: " + nome);
    }

    @Então("{string} deve constar na lista")
    public void deveConstarNaLista(String nome) {
        assertNotNull(listaResponsaveis);
        assertTrue(listaResponsaveis.stream().anyMatch(c -> c.getNomeCompleto().equals(nome)),
                "A lista deveria conter: " + nome);
    }

    @Então("{string} não deve constar na lista")
    public void naoDeveConstarNaLista(String nome) {
        naoDeveAparecerNaLista(nome);
    }

    @Então("{string} deve voltar a aparecer nas listas de seleção")
    public void deveVoltarAAparecer(String nome) {
        assertTrue(service.listarResponsaveisEsterilizacao().stream()
                        .anyMatch(c -> c.getNomeCompleto().equals(nome)),
                nome + " deveria voltar a aparecer nas listas de seleção");
    }

    private void garantirCadastrado(String nome, String funcao) {
        if (!service.existe(nome)) {
            ultimoColaborador = service.cadastrar(nome, cpfFor(nome), "81999990000", funcao);
        } else {
            ultimoColaborador = service.buscarPorNome(nome);
        }
    }

    private void aplicarStatus(String nome, String status) {
        if (StatusColaborador.valueOf(status.toUpperCase()) == StatusColaborador.INATIVO) {
            service.desativar(nome);
        }
        ultimoColaborador = service.buscarPorNome(nome);
    }

    private String cpfFor(String nome) {
        int n = Math.abs(nome.hashCode()) % 1000;
        return String.format("000.000.%03d-00", n);
    }

    private void tentar(Runnable acao) {
        try {
            acao.run();
        } catch (Exception e) {
            SharedTestServices.setLastException(e);
        }
    }
}
