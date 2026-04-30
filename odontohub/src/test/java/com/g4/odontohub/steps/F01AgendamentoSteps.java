package com.g4.odontohub.steps;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.StatusAgendamento;
import com.g4.odontohub.agendamento.domain.model.TipoAtendimento;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class F01AgendamentoSteps {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Exception excecaoCapturada;

    private AgendamentoApplicationService service() {
        return SharedTestServices.getAgendamentoApplicationService();
    }

    private Agendamento ultimoAgendamento() {
        return SharedTestServices.getLastAgendamento();
    }

    private void definirUltimoAgendamento(Agendamento agendamento) {
        SharedTestServices.setLastAgendamento(agendamento);
    }

    @Dado("que o dentista {string} está cadastrado no sistema")
    public void queODentistaEstaCadastrado(String nome) {
        service().cadastrarDentista(nome);
    }

    @Dado("que o paciente {string} está cadastrado no sistema")
    public void queOPacienteEstaCadastrado(String nome) {
        service().cadastrarPaciente(nome);
    }

    @Dado("que o paciente {string} não possui Plano de Tratamento ativo")
    public void pacienteSemPlanoAtivo(String nome) {
        service().definirPlanoAtivo(nome, false);
    }

    @Dado("que o paciente {string} possui um Plano de Tratamento com status {string}")
    public void pacienteComPlanoAtivo(String nome, String status) {
        service().definirPlanoAtivo(nome, true);
    }

    @Dado("que já existe um agendamento confirmado para {string} em {string}")
    public void jaExisteAgendamentoConfirmado(String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        Agendamento ag = service().registrarAgendamento("João Silva", nomeDentista, dataHora);
        service().confirmarAgendamento(ag.getId(), "Sistema");
        definirUltimoAgendamento(ag);
    }

    @Dado("que o paciente {string} possui parcelas vencidas há mais de 30 dias")
    public void pacienteInadimplente(String nome) {
        service().definirInadimplente(nome, true);
    }

    @Dado("que existe um agendamento com status {string} para {string}")
    public void existeAgendamentoComStatus(String status, String nomePaciente) {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(10);
        Agendamento ultimoAgendamento = service().registrarAgendamento(nomePaciente, "Dr. Carlos", dataHora);
        if ("Confirmado".equalsIgnoreCase(status)) {
            service().confirmarAgendamento(ultimoAgendamento.getId(), "Sistema");
        } else if ("Nao Compareceu".equalsIgnoreCase(status)) {
            service().confirmarAgendamento(ultimoAgendamento.getId(), "Sistema");
            service().definirDataHoraAtualParaTestes(dataHora.plusMinutes(1));
            service().marcarNaoComparecimento(ultimoAgendamento.getId(), "Sistema");
            service().limparDataHoraAtualParaTestes();
        }
        definirUltimoAgendamento(ultimoAgendamento);
    }

    @Dado("que existe um agendamento com status {string} para {string} em {string}")
    public void existeAgendamentoComStatusEData(String status, String nomePaciente, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        Agendamento ultimoAgendamento = service().registrarAgendamento(nomePaciente, "Dr. Carlos", dataHora);
        if ("Confirmado".equalsIgnoreCase(status)) {
            service().confirmarAgendamento(ultimoAgendamento.getId(), "Sistema");
        } else if ("Cancelado".equalsIgnoreCase(status)) {
            service().cancelarAgendamento(ultimoAgendamento.getId(), "Cancelamento previo", "Sistema");
        } else if ("Nao Compareceu".equalsIgnoreCase(status)) {
            service().confirmarAgendamento(ultimoAgendamento.getId(), "Sistema");
            service().definirDataHoraAtualParaTestes(dataHora.plusMinutes(1));
            service().marcarNaoComparecimento(ultimoAgendamento.getId(), "Sistema");
            service().limparDataHoraAtualParaTestes();
        }
        definirUltimoAgendamento(ultimoAgendamento);
    }

    @Quando("a recepcionista registra um agendamento para {string} com {string} em {string}")
    public void registrarAgendamento(String nomePaciente, String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        definirUltimoAgendamento(service().registrarAgendamento(nomePaciente, nomeDentista, dataHora));
    }

    @Quando("a recepcionista tenta registrar outro agendamento para {string} em {string}")
    public void tentaRegistrarOutroAgendamento(String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        try {
            definirUltimoAgendamento(service().registrarAgendamento("João Silva", nomeDentista, dataHora));
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @Quando("a recepcionista tenta registrar um agendamento para {string}")
    public void tentaRegistrarAgendamentoPara(String nomePaciente) {
        try {
            definirUltimoAgendamento(service().registrarAgendamento(
                    nomePaciente, "Dr. Carlos", LocalDateTime.now().plusDays(5)));
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @Quando("a recepcionista tenta registrar um agendamento para {string} com {string} em {string}")
    public void tentaRegistrarAgendamento(String nomePaciente, String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        try {
            definirUltimoAgendamento(service().registrarAgendamento(nomePaciente, nomeDentista, dataHora));
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @Quando("a recepcionista {string} confirma o agendamento")
    public void confirmarAgendamento(String responsavel) {
        service().confirmarAgendamento(ultimoAgendamento().getId(), responsavel);
    }

    @Quando("a recepcionista cancela o agendamento informando o motivo {string}")
    public void cancelarAgendamento(String motivo) {
        service().cancelarAgendamento(ultimoAgendamento().getId(), motivo, "Recepcionista");
    }

    @Quando("a recepcionista {string} remarca o agendamento para {string}")
    public void remarcarAgendamento(String responsavel, String novaDataHoraStr) {
        LocalDateTime novaDataHora = LocalDateTime.parse(novaDataHoraStr, FORMATTER);
        service().remarcarAgendamento(ultimoAgendamento().getId(), novaDataHora, responsavel);
    }

    @Então("o agendamento deve ser criado com o tipo {string}")
    public void agendamentoDeveSerCriadoComTipo(String tipoEsperado) {
        assertNotNull(ultimoAgendamento());
        TipoAtendimento esperado = "Consulta".equalsIgnoreCase(tipoEsperado)
                ? TipoAtendimento.CONSULTA : TipoAtendimento.RETORNO;
        assertEquals(esperado, ultimoAgendamento().getTipo());
    }

    @Então("o status do agendamento deve ser {string}")
    public void statusDoAgendamentoDeveSer(String statusEsperado) {
        assertNotNull(ultimoAgendamento());
        assertEquals(mapearStatus(statusEsperado), ultimoAgendamento().getStatus());
    }

    @Então("o sistema deve rejeitar o agendamento")
    public void sistemaDeveRejeitarOAgendamento() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter rejeitado o agendamento");
    }

    @E("o responsável pela alteração deve ser registrado como {string}")
    public void responsavelPelaAlteracao(String responsavel) {
        assertEquals(responsavel, ultimoAgendamento().getResponsavelAlteracao());
    }

    @E("a data da última alteração deve ser registrada")
    public void dataUltimaAlteracaoDeveSerRegistrada() {
        assertNotNull(ultimoAgendamento().getDataUltimaAlteracao());
    }

    @E("o motivo de cancelamento deve ser registrado como {string}")
    public void motivoDeCancelamento(String motivo) {
        assertEquals(motivo, ultimoAgendamento().getMotivoCancelamento());
    }

    @E("a nova data deve ser {string}")
    public void aNovaDatDeveSer(String dataHoraStr) {
        LocalDateTime esperada = LocalDateTime.parse(dataHoraStr, FORMATTER);
        assertEquals(esperada, ultimoAgendamento().getDataHora());
    }

    private StatusAgendamento mapearStatus(String statusStr) {
        return switch (statusStr.toLowerCase()) {
            case "agendado" -> StatusAgendamento.AGENDADO;
            case "confirmado" -> StatusAgendamento.CONFIRMADO;
            case "cancelado" -> StatusAgendamento.CANCELADO;
            case "remarcado" -> StatusAgendamento.REMARCADO;
            case "não compareceu", "nao compareceu" -> StatusAgendamento.NAO_COMPARECEU;
            default -> throw new IllegalArgumentException("Status desconhecido: " + statusStr);
        };
    }
}
