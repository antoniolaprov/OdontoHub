package com.g4.odontohub.steps;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.RespostaPaciente;
import com.g4.odontohub.agendamento.domain.model.StatusAgendamento;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class F13ConfirmacaoLembretesSteps {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private AgendamentoApplicationService service() {
        return SharedTestServices.getAgendamentoApplicationService();
    }

    private Agendamento ultimoAgendamento() {
        return SharedTestServices.getLastAgendamento();
    }

    private void definirUltimoAgendamento(Agendamento agendamento) {
        SharedTestServices.setLastAgendamento(agendamento);
    }

    @Dado("que o dentista {string} esta cadastrado no sistema")
    public void queODentistaEstaCadastradoNoSistemaSemAcento(String nome) {
        service().cadastrarDentista(nome);
    }

    @Dado("que o paciente {string} esta cadastrado no sistema")
    public void queOPacienteEstaCadastradoNoSistemaSemAcento(String nome) {
        service().cadastrarPaciente(nome);
    }

    @Dado("que {string} recebeu um lembrete para o agendamento de {string}")
    public void queRecebeuUmLembreteParaOAgendamentoDe(String nomePaciente, String dataHoraStr) {
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        Agendamento agendamento = service().registrarAgendamento(nomePaciente, "Dr. Carlos", dataHora);
        service().confirmarAgendamento(agendamento.getId(), "Sistema");
        service().enviarLembreteConsulta(agendamento.getId(), "WhatsApp");
        definirUltimoAgendamento(service().obterAgendamento(agendamento.getId()));
    }

    @Dado("que ja foi enviado um lembrete para {string} nas ultimas 24 horas")
    public void queJaFoiEnviadoUmLembreteParaNasUltimas24Horas(String nomePaciente) {
        LocalDateTime dataHora = LocalDateTime.parse("15/05/2026 14:00", FORMATTER);
        Agendamento agendamento = service().registrarAgendamento(nomePaciente, "Dr. Carlos", dataHora);
        service().confirmarAgendamento(agendamento.getId(), "Sistema");
        service().enviarLembreteConsulta(agendamento.getId(), "WhatsApp");
        definirUltimoAgendamento(service().obterAgendamento(agendamento.getId()));
    }

    @Quando("o sistema envia o lembrete de consulta pelo canal {string}")
    public void oSistemaEnviaOLembreteDeConsultaPeloCanal(String canal) {
        service().enviarLembreteConsulta(ultimoAgendamento().getId(), canal);
        definirUltimoAgendamento(service().obterAgendamento(ultimoAgendamento().getId()));
    }

    @Quando("o sistema registra a confirmacao de presenca de {string}")
    public void oSistemaRegistraAConfirmacaoDePresencaDe(String nomePaciente) {
        service().registrarConfirmacaoPaciente(ultimoAgendamento().getId());
        definirUltimoAgendamento(service().buscarAgendamentoPorPacienteEData(nomePaciente, ultimoAgendamento().getDataHora()));
    }

    @Quando("a recepcionista registra a recusa de {string} com o motivo {string}")
    public void aRecepcionistaRegistraARecusaDeComOMotivo(String nomePaciente, String motivo) {
        service().registrarRecusaPaciente(ultimoAgendamento().getId(), motivo, "Recepcionista");
        definirUltimoAgendamento(service().buscarAgendamentoPorPacienteEData(nomePaciente, ultimoAgendamento().getDataHora()));
    }

    @Quando("o sistema tenta enviar o lembrete de consulta")
    public void oSistemaTentaEnviarOLembreteDeConsulta() {
        try {
            service().enviarLembreteConsulta(ultimoAgendamento().getId(), "WhatsApp");
        } catch (Exception e) {
            SharedTestServices.setLastException(e);
        }
    }

    @Quando("o sistema tenta enviar um novo lembrete para o mesmo agendamento")
    public void oSistemaTentaEnviarUmNovoLembreteParaOMesmoAgendamento() {
        try {
            service().enviarLembreteConsulta(ultimoAgendamento().getId(), "SMS");
        } catch (Exception e) {
            SharedTestServices.setLastException(e);
        }
    }

    @Então("o lembrete deve ser registrado para o agendamento")
    public void oLembreteDeveSerRegistradoParaOAgendamento() {
        assertNotNull(ultimoAgendamento().getDataUltimoLembrete());
    }

    @E("o canal do ultimo lembrete deve ser {string}")
    public void oCanalDoUltimoLembreteDeveSer(String canal) {
        assertEquals(canal, ultimoAgendamento().getCanalUltimoLembrete());
    }

    @E("a resposta do paciente deve permanecer como {string}")
    public void aRespostaDoPacienteDevePermanecerComo(String resposta) {
        assertEquals(mapearResposta(resposta), ultimoAgendamento().getRespostaPaciente());
    }

    @Então("a resposta do paciente deve ser {string}")
    public void aRespostaDoPacienteDeveSer(String resposta) {
        assertEquals(mapearResposta(resposta), ultimoAgendamento().getRespostaPaciente());
    }

    @E("a data da confirmacao do paciente deve ser registrada")
    public void aDataDaConfirmacaoDoPacienteDeveSerRegistrada() {
        assertNotNull(ultimoAgendamento().getDataConfirmacaoPaciente());
    }

    @E("o agendamento deve ser cancelado com o motivo {string}")
    public void oAgendamentoDeveSerCanceladoComOMotivo(String motivo) {
        assertEquals(StatusAgendamento.CANCELADO, ultimoAgendamento().getStatus());
        assertEquals(motivo, ultimoAgendamento().getMotivoCancelamento());
    }

    @Então("o sistema deve rejeitar o envio do lembrete")
    public void oSistemaDeveRejeitarOEnvioDoLembrete() {
        assertNotNull(SharedTestServices.getLastException());
    }

    @Então("o sistema deve impedir o envio duplicado")
    public void oSistemaDeveImpedirOEnvioDuplicado() {
        assertNotNull(SharedTestServices.getLastException());
    }

    private RespostaPaciente mapearResposta(String resposta) {
        return switch (resposta.toLowerCase()) {
            case "pendente" -> RespostaPaciente.PENDENTE;
            case "confirmado" -> RespostaPaciente.CONFIRMADO;
            case "recusado" -> RespostaPaciente.RECUSADO;
            default -> throw new IllegalArgumentException("Resposta desconhecida: " + resposta);
        };
    }
}
