package com.g4.odontohub.steps;

import com.g4.odontohub.clinica.application.ClinicaApplicationService;
import com.g4.odontohub.clinica.domain.model.Clinica;
import com.g4.odontohub.clinica.domain.model.StatusClinica;
import io.cucumber.java.pt.*;

import static org.junit.jupiter.api.Assertions.*;

public class F18ClinicaSteps {

    private final ClinicaApplicationService service = new ClinicaApplicationService();
    private Clinica ultimaClinica;
    private Clinica clinicaAutenticada;
    private Exception excecaoCapturada;

    // ---------- Cadastro ----------

    @Quando("o administrador cadastra a clínica {string} com CNPJ {string} email {string} e senha {string}")
    public void cadastrarClinica(String nome, String cnpj, String email, String senha) {
        ultimaClinica = service.cadastrar(nome, cnpj, email, senha);
    }

    @Quando("o administrador tenta cadastrar uma clínica sem informar o nome")
    public void tentarCadastrarSemNome() {
        tentarExecutar(() -> ultimaClinica = service.cadastrar(
                "", "22.222.222/0001-22", "novo@clinica.com", "segredo123"));
    }

    @Quando("o administrador tenta cadastrar uma clínica com o email {string}")
    public void tentarCadastrarEmailInvalido(String email) {
        tentarExecutar(() -> ultimaClinica = service.cadastrar(
                "Clínica Teste", "22.222.222/0001-22", email, "segredo123"));
    }

    @Quando("o administrador tenta cadastrar uma clínica com a senha {string}")
    public void tentarCadastrarSenhaCurta(String senha) {
        tentarExecutar(() -> ultimaClinica = service.cadastrar(
                "Clínica Teste", "22.222.222/0001-22", "teste@clinica.com", senha));
    }

    @Dado("que já existe uma clínica cadastrada com email {string}")
    public void jaExisteClinicaComEmail(String email) {
        service.cadastrar("Clínica Existente", "33.333.333/0001-33", email, "segredo123");
    }

    @Quando("o administrador tenta cadastrar outra clínica com email {string}")
    public void tentarCadastrarEmailDuplicado(String email) {
        tentarExecutar(() -> ultimaClinica = service.cadastrar(
                "Outra Clínica", "44.444.444/0001-44", email, "segredo123"));
    }

    @Dado("que já existe uma clínica cadastrada com CNPJ {string}")
    public void jaExisteClinicaComCnpj(String cnpj) {
        service.cadastrar("Clínica Existente", cnpj, "existente@clinica.com", "segredo123");
    }

    @Quando("o administrador tenta cadastrar outra clínica com CNPJ {string}")
    public void tentarCadastrarCnpjDuplicado(String cnpj) {
        tentarExecutar(() -> ultimaClinica = service.cadastrar(
                "Outra Clínica", cnpj, "outra@clinica.com", "segredo123"));
    }

    // ---------- Login ----------

    @Dado("que existe uma clínica cadastrada com email {string} e senha {string}")
    public void existeClinicaComEmailESenha(String email, String senha) {
        ultimaClinica = service.cadastrar("Sorriso Feliz", "11.111.111/0001-11", email, senha);
    }

    @Quando("a clínica faz login com email {string} e senha {string}")
    public void clinicaFazLogin(String email, String senha) {
        clinicaAutenticada = service.autenticar(email, senha);
    }

    @Quando("a clínica tenta fazer login com email {string} e senha {string}")
    public void clinicaTentaLogin(String email, String senha) {
        tentarExecutar(() -> clinicaAutenticada = service.autenticar(email, senha));
    }

    // ---------- Verificações ----------

    @Então("a clínica deve ser salva no sistema")
    public void clinicaDeveSerSalva() {
        assertNotNull(ultimaClinica);
        assertNotNull(ultimaClinica.getId());
        assertTrue(service.emailJaCadastrado(ultimaClinica.getEmail()));
    }

    @Então("o status da clínica deve ser {string}")
    public void statusClinicaDeveSer(String status) {
        assertNotNull(ultimaClinica);
        assertEquals(StatusClinica.valueOf(status), ultimaClinica.getStatus());
    }

    @Então("a senha da clínica não deve ser armazenada em texto puro")
    public void senhaNaoDeveSerTextoPuro() {
        assertNotNull(ultimaClinica.getSenhaHash());
        assertNotEquals("segredo123", ultimaClinica.getSenhaHash());
        assertTrue(ultimaClinica.getSenhaHash().startsWith("$2"), "Hash deve ser BCrypt");
    }

    @Então("o sistema deve rejeitar o cadastro da clínica")
    public void sistemaDeveRejeitarCadastroClinica() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter rejeitado o cadastro da clínica");
    }

    @Então("a mensagem de erro da clínica deve informar {string}")
    public void mensagemErroDeveInformar(String mensagem) {
        assertNotNull(excecaoCapturada);
        assertEquals(mensagem, excecaoCapturada.getMessage());
    }

    @Então("o login da clínica deve ser bem-sucedido")
    public void loginDeveSerBemSucedido() {
        assertNotNull(clinicaAutenticada);
        assertNull(excecaoCapturada);
    }

    @Então("a clínica autenticada deve ser {string}")
    public void clinicaAutenticadaDeveSer(String nome) {
        assertNotNull(clinicaAutenticada);
        assertEquals(nome, clinicaAutenticada.getNome());
    }

    @Então("o login da clínica deve ser rejeitado")
    public void loginDeveSerRejeitado() {
        assertNull(clinicaAutenticada);
        assertNotNull(excecaoCapturada, "O login deveria ter sido rejeitado");
    }

    private void tentarExecutar(Operacao operacao) {
        try {
            excecaoCapturada = null;
            operacao.executar();
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @FunctionalInterface
    private interface Operacao {
        void executar();
    }
}
