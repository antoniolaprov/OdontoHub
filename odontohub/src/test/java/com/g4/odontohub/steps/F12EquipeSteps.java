package com.g4.odontohub.steps;

import com.g4.odontohub.equipe.application.ColaboradorApplicationService;
import com.g4.odontohub.equipe.domain.model.*;
import io.cucumber.java.pt.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class F12EquipeSteps {

    private final ColaboradorApplicationService service = new ColaboradorApplicationService();
    private Colaborador ultimoColaborador;
    private List<Colaborador> listaResponsaveis;
    private boolean ultimoLoginOk;

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

    // ----- Permissões por função -----

    @Então("o colaborador {string} deve poder validar procedimentos")
    public void devePoderValidarProcedimentos(String nome) {
        assertTrue(service.buscarPorNome(nome).podeValidarProcedimentos(),
                nome + " deveria poder validar procedimentos");
    }

    @Então("o colaborador {string} deve poder acessar dados financeiros")
    public void devePoderAcessarFinanceiro(String nome) {
        assertTrue(service.buscarPorNome(nome).podeAcessarFinanceiro(),
                nome + " deveria poder acessar dados financeiros");
    }

    @Então("o colaborador {string} não deve poder acessar dados financeiros")
    public void naoDevePoderAcessarFinanceiro(String nome) {
        assertFalse(service.buscarPorNome(nome).podeAcessarFinanceiro(),
                nome + " não deveria poder acessar dados financeiros");
    }

    @Então("o colaborador {string} deve poder alterar permissões")
    public void devePoderAlterarPermissoes(String nome) {
        assertTrue(service.buscarPorNome(nome).podeAlterarPermissoes(),
                nome + " deveria poder alterar permissões");
    }

    @Então("o colaborador {string} não deve poder alterar permissões")
    public void naoDevePoderAlterarPermissoes(String nome) {
        assertFalse(service.buscarPorNome(nome).podeAlterarPermissoes(),
                nome + " não deveria poder alterar permissões");
    }

    // ----- Login e segurança -----

    @Dado("que existe o colaborador {string} com login {string} e senha {string}")
    public void existeColaboradorComLoginSenha(String nome, String login, String senha) {
        ultimoColaborador = service.cadastrarCompleto(nome, cpfFor(nome), "81999990000",
                login + "@odontohub.com", login, senha, "Recepcionista");
    }

    @Dado("que existe o colaborador {string} com CPF {string}")
    public void existeColaboradorComCpf(String nome, String cpf) {
        ultimoColaborador = service.cadastrar(nome, cpf, "81999990000", "Auxiliar");
    }

    @Quando("o dentista tenta cadastrar outro colaborador com o CPF {string}")
    public void tentaCadastrarComCpfDuplicado(String cpf) {
        tentar(() -> service.cadastrar("Outro Colaborador", cpf, "81999990000", "Auxiliar"));
    }

    @Quando("{string} tenta fazer login com a senha {string}")
    public void tentaFazerLogin(String nome, String senha) {
        ultimoLoginOk = service.tentarLogin(nome, senha);
    }

    @Quando("{string} erra a senha {int} vezes")
    public void erraASenha(String nome, int vezes) {
        for (int i = 0; i < vezes; i++) {
            ultimoLoginOk = service.tentarLogin(nome, "senha-errada");
        }
    }

    @Então("o login deve ser negado")
    public void loginDeveSerNegado() {
        assertFalse(ultimoLoginOk, "O login deveria ter sido negado");
    }

    @Então("a conta de {string} deve estar bloqueada")
    public void contaDeveEstarBloqueada(String nome) {
        assertTrue(service.estaBloqueado(nome), "A conta de " + nome + " deveria estar bloqueada");
    }

    @Então("{string} não deve conseguir login mesmo com a senha correta {string}")
    public void naoDeveLogarMesmoComSenhaCorreta(String nome, String senhaCorreta) {
        assertFalse(service.tentarLogin(nome, senhaCorreta),
                nome + " não deveria conseguir login com a conta bloqueada");
    }

    // ----- Disponibilidade, auditoria, alterações e indicadores (F12) -----

    @Dado("que o colaborador {string} está cadastrado com função {string}")
    public void colaboradorCadastradoComFuncao(String nome, String funcao) {
        garantirCadastrado(nome, funcao);
    }

    @Dado("que {string} tem disponibilidade de {string} das {int}h às {int}h")
    public void temDisponibilidade(String nome, String diasCsv, int horaInicio, int horaFim) {
        Set<DiaSemana> dias = new LinkedHashSet<>();
        for (String dia : diasCsv.split(",")) {
            dias.add(DiaSemana.fromLabel(dia));
        }
        service.definirDisponibilidade(nome, dias, horaInicio, horaFim);
    }

    @Dado("que {string} registra ausência do tipo {string} de {string} a {string}")
    public void registraAusencia(String nome, String tipo, String inicio, String fim) {
        service.registrarAusencia(nome, LocalDate.parse(inicio), LocalDate.parse(fim), tipo, "ausência");
    }

    @Então("{string} deve estar disponível em {string}")
    public void deveEstarDisponivel(String nome, String momento) {
        assertTrue(service.estaDisponivelEm(nome, LocalDateTime.parse(momento)),
                nome + " deveria estar disponível em " + momento);
    }

    @Então("{string} não deve estar disponível em {string}")
    public void naoDeveEstarDisponivel(String nome, String momento) {
        assertFalse(service.estaDisponivelEm(nome, LocalDateTime.parse(momento)),
                nome + " não deveria estar disponível em " + momento);
    }

    @Quando("{string} registra a ação {string} no módulo {string}")
    public void registraAcao(String nome, String acao, String modulo) {
        service.registrarAcao(nome, modulo, acao);
    }

    @Então("a auditoria de {string} deve conter {int} registros")
    public void auditoriaDeveConter(String nome, int quantidade) {
        assertEquals(quantidade, service.auditoriaDe(nome).size());
    }

    @Então("a rastreabilidade de {string} no módulo {string} deve conter {int} registro")
    public void rastreabilidadeDeveConter(String nome, String modulo, int quantidade) {
        assertEquals(quantidade, service.rastrearAcoes(nome, modulo).size());
    }

    @Quando("o administrador altera o campo {string} de {string} para {string} como {string}")
    public void administradorAlteraCampo(String campo, String nome, String novoValor, String responsavel) {
        service.alterarDado(nome, campo, novoValor, responsavel);
    }

    @Então("o histórico de alterações de {string} deve conter {int} registro de alteração")
    public void historicoAlteracoesDeveConter(String nome, int quantidade) {
        assertEquals(quantidade, service.historicoAlteracoesDe(nome).size());
    }

    @Então("o telefone de {string} deve ser {string}")
    public void telefoneDeveSer(String nome, String telefone) {
        assertEquals(telefone, service.buscarPorNome(nome).getTelefone());
    }

    @Quando("{string} registra {int} atendimentos, {int} falta e {int} conversões")
    public void registraDesempenho(String nome, int atendimentos, int faltas, int conversoes) {
        for (int i = 0; i < atendimentos; i++) service.registrarAtendimento(nome);
        for (int i = 0; i < faltas; i++) service.registrarFalta(nome);
        for (int i = 0; i < conversoes; i++) service.registrarConversao(nome);
    }

    @Então("a produtividade de {string} deve ser {int}")
    public void produtividadeDeveSer(String nome, int esperado) {
        assertEquals(esperado, service.indicadoresDe(nome).produtividade());
    }

    @Então("a taxa de conversão do colaborador {string} deve ser {int}%")
    public void taxaConversaoColaborador(String nome, int percentual) {
        assertEquals(percentual / 100.0, service.indicadoresDe(nome).taxaConversao(), 0.001);
    }

    private void aplicarStatus(String nome, String status) {
        StatusColaborador alvo = StatusColaborador.fromLabel(status);
        if (alvo != StatusColaborador.ATIVO) {
            service.alterarStatus(nome, status);
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
