package com.g4.odontohub.steps;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.StatusAgendamento;
import com.g4.odontohub.agendamento.domain.model.TipoAtendimento;
import io.cucumber.java.pt.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class F01AgendamentoSteps {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AgendamentoApplicationService service = new AgendamentoApplicationService();
    private Agendamento ultimoAgendamento;
    private Exception excecaoCapturada;

    @Dado("que o dentista {string} está cadastrado no sistema")
    public void queODentistaEstaCadastrado(String nome) {
        service.cadastrarDentista(nome);
    }

    @Dado("que o paciente {string} está cadastrado no sistema")
    public void queOPacienteEstaCadastrado(String nome) {
        service.cadastrarPaciente(nome);
    }

    @Dado("que o paciente {string} não possui Plano de Tratamento ativo")
    public void pacienteSemPlanoAtivo(String nome) {
        service.definirPlanoAtivo(nome, false);
    }

    @Dado("que o paciente {string} possui um Plano de Tratamento com status {string}")
    public void pacienteComPlanoAtivo(String nome, String status) {
        service.definirPlanoAtivo(nome, true);
    }

    @Dado("que já existe um agendamento confirmado para {string} em {string}")
    public void jaExisteAgendamentoConfirmado(String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        Agendamento ag = service.registrarAgendamento("João Silva", nomeDentista, dataHora);
        service.confirmarAgendamento(ag.getId(), "Sistema");
        ultimoAgendamento = ag;
    }

    @Dado("que o paciente {string} possui parcelas vencidas há mais de 30 dias")
    public void pacienteInadimplente(String nome) {
        service.definirInadimplente(nome, true);
    }

    @Dado("que existe um agendamento com status {string} para {string}")
    public void existeAgendamentoComStatus(String status, String nomePaciente) {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(10);
        ultimoAgendamento = service.registrarAgendamento(nomePaciente, "Dr. Carlos", dataHora);
        if ("Confirmado".equalsIgnoreCase(status)) {
            service.confirmarAgendamento(ultimoAgendamento.getId(), "Sistema");
        }
    }

    @Dado("que existe um agendamento com status {string} para {string} em {string}")
    public void existeAgendamentoComStatusEData(String status, String nomePaciente, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        ultimoAgendamento = service.registrarAgendamento(nomePaciente, "Dr. Carlos", dataHora);
        if ("Confirmado".equalsIgnoreCase(status)) {
            service.confirmarAgendamento(ultimoAgendamento.getId(), "Sistema");
        }
    }

    @Quando("a recepcionista registra um agendamento para {string} com {string} em {string}")
    public void registrarAgendamento(String nomePaciente, String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        ultimoAgendamento = service.registrarAgendamento(nomePaciente, nomeDentista, dataHora);
    }

    @Quando("a recepcionista tenta registrar outro agendamento para {string} em {string}")
    public void tentaRegistrarOutroAgendamento(String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        try {
            ultimoAgendamento = service.registrarAgendamento("João Silva", nomeDentista, dataHora);
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @Quando("a recepcionista tenta registrar um agendamento para {string}")
    public void tentaRegistrarAgendamentoPara(String nomePaciente) {
        try {
            ultimoAgendamento = service.registrarAgendamento(
                    nomePaciente, "Dr. Carlos", LocalDateTime.now().plusDays(5));
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @Quando("a recepcionista tenta registrar um agendamento para {string} com {string} em {string}")
    public void tentaRegistrarAgendamento(String nomePaciente, String nomeDentista, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        try {
            ultimoAgendamento = service.registrarAgendamento(nomePaciente, nomeDentista, dataHora);
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @Quando("a recepcionista {string} confirma o agendamento")
    public void confirmarAgendamento(String responsavel) {
        service.confirmarAgendamento(ultimoAgendamento.getId(), responsavel);
    }

    @Quando("a recepcionista cancela o agendamento informando o motivo {string}")
    public void cancelarAgendamento(String motivo) {
        service.cancelarAgendamento(ultimoAgendamento.getId(), motivo, "Recepcionista");
    }

    @Quando("a recepcionista {string} remarca o agendamento para {string}")
    public void remarcarAgendamento(String responsavel, String novaDataHoraStr) {
        LocalDateTime novaDataHora = LocalDateTime.parse(novaDataHoraStr, FORMATTER);
        service.remarcarAgendamento(ultimoAgendamento.getId(), novaDataHora, responsavel);
    }

    @Então("o agendamento deve ser criado com o tipo {string}")
    public void agendamentoDeveSerCriadoComTipo(String tipoEsperado) {
        assertNotNull(ultimoAgendamento);
        TipoAtendimento esperado = "Consulta".equalsIgnoreCase(tipoEsperado)
                ? TipoAtendimento.CONSULTA : TipoAtendimento.RETORNO;
        assertEquals(esperado, ultimoAgendamento.getTipo());
    }

    @Então("o status do agendamento deve ser {string}")
    public void statusDoAgendamentoDeveSer(String statusEsperado) {
        assertNotNull(ultimoAgendamento);
        assertEquals(mapearStatus(statusEsperado), ultimoAgendamento.getStatus());
    }

    @Então("o sistema deve rejeitar o agendamento")
    public void sistemaDeveRejeitarOAgendamento() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter rejeitado o agendamento");
    }

    @E("o responsável pela alteração deve ser registrado como {string}")
    public void responsavelPelaAlteracao(String responsavel) {
        assertEquals(responsavel, ultimoAgendamento.getResponsavelAlteracao());
    }

    @E("a data da última alteração deve ser registrada")
    public void dataUltimaAlteracaoDeveSerRegistrada() {
        assertNotNull(ultimoAgendamento.getDataUltimaAlteracao());
    }

    @E("o motivo de cancelamento deve ser registrado como {string}")
    public void motivoDeCancelamento(String motivo) {
        assertEquals(motivo, ultimoAgendamento.getMotivoCancelamento());
    }

    @E("a nova data deve ser {string}")
    public void aNovaDatDeveSer(String dataHoraStr) {
        LocalDateTime esperada = LocalDateTime.parse(dataHoraStr, FORMATTER);
        assertEquals(esperada, ultimoAgendamento.getDataHora());
    }

    private StatusAgendamento mapearStatus(String statusStr) {
        return switch (statusStr.toLowerCase()) {
            case "agendado"   -> StatusAgendamento.AGENDADO;
            case "confirmado" -> StatusAgendamento.CONFIRMADO;
            case "cancelado"  -> StatusAgendamento.CANCELADO;
            case "remarcado"  -> StatusAgendamento.REMARCADO;
            default -> throw new IllegalArgumentException("Status desconhecido: " + statusStr);
        };
    }
}