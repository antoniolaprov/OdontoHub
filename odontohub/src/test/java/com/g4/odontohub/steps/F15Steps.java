package com.g4.odontohub.steps;

import com.g4.odontohub.cadastropaciente.application.PacienteApplicationService;
import com.g4.odontohub.cadastropaciente.domain.model.AlteracaoCadastral;
import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.StatusPaciente;
import io.cucumber.java.pt.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class F15Steps {

    private final PacienteApplicationService service = new PacienteApplicationService();
    private Paciente ultimoPaciente;
    private Exception excecaoCapturada;

    @Quando("a recepcionista cadastra o paciente {string} com CPF {string} telefone {string} nascimento {string} e email {string}")
    public void cadastrarPacienteCompleto(String nome, String cpf, String telefone, String nascimento, String email) {
        ultimoPaciente = service.cadastrarCompleto(nome, cpf, nascimento, telefone, email, "Recepcionista");
    }

    @Quando("a recepcionista realiza um cadastro rápido com nome {string} e telefone {string}")
    public void cadastrarPacienteRapido(String nome, String telefone) {
        ultimoPaciente = service.cadastrarRapido(nome, telefone, "Recepcionista");
    }

    @Quando("a recepcionista tenta cadastrar um paciente completo sem informar o CPF")
    public void tentarCadastrarSemCpf() {
        tentarExecutar(() -> ultimoPaciente = service.cadastrarCompleto(
                "Ana Costa", "", "12/03/1988", "81 9 9999-1111", "ana.costa@email.com", "Recepcionista"));
    }

    @Quando("a recepcionista tenta cadastrar um paciente sem informar o nome")
    public void tentarCadastrarSemNome() {
        tentarExecutar(() -> ultimoPaciente = service.cadastrarCompleto(
                "", "123.456.789-00", "12/03/1988", "81 9 9999-1111", "ana.costa@email.com", "Recepcionista"));
    }

    @Quando("a recepcionista tenta cadastrar um paciente sem informar o telefone")
    public void tentarCadastrarSemTelefone() {
        tentarExecutar(() -> ultimoPaciente = service.cadastrarCompleto(
                "Ana Costa", "123.456.789-00", "12/03/1988", "", "ana.costa@email.com", "Recepcionista"));
    }

    @Dado("que já existe um paciente cadastrado com CPF {string}")
    public void jaExistePacienteComCpf(String cpf) {
        service.cadastrarCompleto("Paciente Existente", cpf, "01/01/1990", "81 9 9999-1111",
                "existente.cpf@email.com", "Recepcionista");
    }

    @Quando("a recepcionista tenta cadastrar outro paciente com CPF {string}")
    public void tentarCadastrarCpfDuplicado(String cpf) {
        tentarExecutar(() -> ultimoPaciente = service.cadastrarCompleto(
                "Outro Paciente", cpf, "02/02/1991", "81 9 9999-2222", "outro@email.com", "Recepcionista"));
    }

    @Dado("que já existe um paciente cadastrado com email {string}")
    public void jaExistePacienteComEmail(String email) {
        service.cadastrarCompleto("Paciente Existente", "123.456.789-00", "01/01/1990",
                "81 9 9999-1111", email, "Recepcionista");
    }

    @Quando("a recepcionista tenta cadastrar outro paciente com email {string}")
    public void tentarCadastrarEmailDuplicado(String email) {
        tentarExecutar(() -> ultimoPaciente = service.cadastrarCompleto(
                "Outro Paciente", "987.654.321-00", "02/02/1991", "81 9 9999-2222", email, "Recepcionista"));
    }

    @Dado("que existe um paciente cadastrado com nome {string} e telefone {string}")
    public void existePacienteComNomeETelefone(String nome, String telefone) {
        ultimoPaciente = service.cadastrarRapido(nome, telefone, "Recepcionista");
    }

    @Quando("a recepcionista atualiza o telefone do paciente para {string} com responsável {string}")
    public void atualizarTelefonePaciente(String novoTelefone, String responsavel) {
        ultimoPaciente = service.atualizarCadastro(ultimoPaciente.getId(), "telefone", novoTelefone, responsavel);
    }

    @Dado("que existe um paciente cadastrado com nome {string} e status {string}")
    public void existePacienteComNomeEStatus(String nome, String status) {
        if (StatusPaciente.valueOf(status) == StatusPaciente.INCOMPLETO) {
            ultimoPaciente = service.cadastrarRapido(nome, "81 9 9999-1111", "Recepcionista");
        } else {
            ultimoPaciente = service.cadastrarCompleto(nome, "123.456.789-00", "01/01/1990",
                    "81 9 9999-1111", "paciente.status@email.com", "Recepcionista");
        }
    }

    @Quando("a recepcionista restringe o paciente {string}")
    public void restringirPaciente(String nome) {
        Paciente paciente = service.buscarPorNome(nome);
        ultimoPaciente = service.restringirPaciente(paciente.getId());
    }

    @Então("o paciente deve ser salvo no sistema")
    public void pacienteDeveSerSalvo() {
        assertNotNull(ultimoPaciente);
        assertSame(ultimoPaciente, service.buscarPorNome(ultimoPaciente.getNomeCompleto()));
    }

    @Então("o status do paciente deve ser {string}")
    public void statusPacienteDeveSer(String status) {
        assertNotNull(ultimoPaciente);
        assertEquals(StatusPaciente.valueOf(status), ultimoPaciente.getStatus());
    }

    @Então("o sistema deve rejeitar o cadastro")
    public void sistemaDeveRejeitarCadastro() {
        Exception ex = excecaoCapturada != null ? excecaoCapturada : SharedTestServices.getLastException();
        assertNotNull(ex, "O sistema deveria ter rejeitado o cadastro");
    }

    @Então("o cadastro do paciente deve refletir o novo telefone {string}")
    public void cadastroDeveRefletirNovoTelefone(String telefone) {
        assertEquals(telefone, ultimoPaciente.getTelefone());
    }

    @Então("o histórico de alterações deve conter o campo {string} com valor anterior {string} e valor atualizado {string}")
    public void historicoDeveConterAlteracao(String campo, String valorAnterior, String valorAtualizado) {
        assertTrue(ultimoPaciente.getHistoricoAlteracoes().stream()
                .anyMatch(a -> a.campo().equals(campo)
                        && a.valorAnterior().equals(valorAnterior)
                        && a.valorAtualizado().equals(valorAtualizado)));
    }

    @Então("o responsável pela alteração deve ser registrado como {string}")
    public void responsavelAlteracaoDeveSerRegistrado(String responsavel) {
        AlteracaoCadastral alteracao = ultimaAlteracao();
        assertEquals(responsavel, alteracao.responsavel());
    }

    @Então("a data da alteração deve ser registrada")
    public void dataAlteracaoDeveSerRegistrada() {
        AlteracaoCadastral alteracao = ultimaAlteracao();
        assertNotNull(alteracao.dataAlteracao());
        assertEquals(LocalDate.now(), alteracao.dataAlteracao());
    }

    private AlteracaoCadastral ultimaAlteracao() {
        assertFalse(ultimoPaciente.getHistoricoAlteracoes().isEmpty());
        return ultimoPaciente.getHistoricoAlteracoes()
                .get(ultimoPaciente.getHistoricoAlteracoes().size() - 1);
    }

    private void tentarExecutar(Operacao operacao) {
        try {
            excecaoCapturada = null;
            operacao.executar();
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @FunctionalInterface
    private interface Operacao {
        void executar();
    }
}
