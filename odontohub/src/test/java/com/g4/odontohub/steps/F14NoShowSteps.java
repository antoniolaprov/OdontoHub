package com.g4.odontohub.steps;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.model.Agendamento;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class F14NoShowSteps {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Exception excecaoCapturada;
    private AgendamentoApplicationService.ResumoOcorrenciasAgenda resumoOcorrencias;
    private String sinalizacaoPaciente;

    private AgendamentoApplicationService service() {
        return SharedTestServices.getAgendamentoApplicationService();
    }

    private Agendamento ultimoAgendamento() {
        return SharedTestServices.getLastAgendamento();
    }

    private void definirUltimoAgendamento(Agendamento agendamento) {
        SharedTestServices.setLastAgendamento(agendamento);
    }

    @Quando("a recepcionista {string} marca o agendamento como {string}")
    public void aRecepcionistaMarcaOAgendamentoComo(String responsavel, String status) {
        if (!"Nao Compareceu".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Status nao suportado no cenario: " + status);
        }
        service().definirDataHoraAtualParaTestes(ultimoAgendamento().getDataHora().plusMinutes(1));
        service().marcarNaoComparecimento(ultimoAgendamento().getId(), responsavel);
        definirUltimoAgendamento(service().obterAgendamento(ultimoAgendamento().getId()));
        service().limparDataHoraAtualParaTestes();
    }

    @Quando("a recepcionista tenta marcar no-show antes do horario da consulta")
    public void aRecepcionistaTentaMarcarNoShowAntesDoHorarioDaConsulta() {
        try {
            service().definirDataHoraAtualParaTestes(ultimoAgendamento().getDataHora().minusMinutes(1));
            service().marcarNaoComparecimento(ultimoAgendamento().getId(), "Ana");
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        } finally {
            service().limparDataHoraAtualParaTestes();
        }
    }

    @Entao("o sistema deve rejeitar a marcacao de no-show")
    public void oSistemaDeveRejeitarAMarcacaoDeNoShow() {
        assertNotNull(excecaoCapturada, "A marcacao de no-show deveria ter sido rejeitada");
    }

    @E("o responsavel pela alteracao deve ser registrado como {string}")
    public void oResponsavelPelaAlteracaoDeveSerRegistradoComoSemAcento(String responsavel) {
        assertEquals(responsavel, ultimoAgendamento().getResponsavelAlteracao());
    }

    @Dado("que {string} possui um agendamento cancelado e um agendamento marcado como {string}")
    public void quePossuiUmAgendamentoCanceladoEUmAgendamentoMarcadoComo(String nomePaciente, String status) {
        LocalDateTime dataCancelamento = LocalDateTime.parse("05/05/2026 09:00", FORMATTER);
        Agendamento cancelado = service().registrarAgendamento(nomePaciente, "Dr. Carlos", dataCancelamento);
        service().confirmarAgendamento(cancelado.getId(), "Sistema");
        service().cancelarAgendamento(cancelado.getId(), "Cancelamento previo", "Sistema");

        LocalDateTime dataNoShow = LocalDateTime.parse("10/05/2026 09:00", FORMATTER);
        Agendamento noShow = service().registrarAgendamento(nomePaciente, "Dr. Carlos", dataNoShow);
        service().confirmarAgendamento(noShow.getId(), "Sistema");
        service().definirDataHoraAtualParaTestes(dataNoShow.plusMinutes(1));
        service().marcarNaoComparecimento(noShow.getId(), "Ana");
        service().limparDataHoraAtualParaTestes();

        definirUltimoAgendamento(service().obterAgendamento(noShow.getId()));
        if (!"Nao Compareceu".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Status nao suportado no cenario: " + status);
        }
    }

    @Quando("o dentista consulta o resumo de ocorrencias da agenda")
    public void oDentistaConsultaOResumoDeOcorrenciasDaAgenda() {
        resumoOcorrencias = service().consultarResumoOcorrencias("Joao Silva");
    }

    @Entao("a quantidade de no-show deve ser exibida separadamente da quantidade de cancelamentos")
    public void aQuantidadeDeNoShowDeveSerExibidaSeparadamenteDaQuantidadeDeCancelamentos() {
        assertNotNull(resumoOcorrencias);
        assertEquals(1L, resumoOcorrencias.quantidadeNoShows());
        assertEquals(1L, resumoOcorrencias.quantidadeCancelamentos());
    }

    @Dado("que {string} possui {int} registros anteriores de no-show nos ultimos {int} meses")
    public void quePossuiRegistrosAnterioresDeNoShowNosUltimosMeses(String nomePaciente, Integer quantidade, Integer meses) {
        LocalDateTime dataBase = LocalDateTime.now()
                .plusMonths(5)
                .withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        for (int i = 0; i < quantidade; i++) {
            LocalDateTime dataConsulta = dataBase.minusMonths((long) meses - 2L - (i * 2L)).plusDays(i + 1L);
            Agendamento agendamento = service().registrarAgendamento(nomePaciente, "Dr. Carlos", dataConsulta);
            service().confirmarAgendamento(agendamento.getId(), "Sistema");
            service().definirDataHoraAtualParaTestes(dataConsulta.plusMinutes(1));
            service().marcarNaoComparecimento(agendamento.getId(), "Sistema");
            service().limparDataHoraAtualParaTestes();
        }

        Agendamento novoAgendamento = service().registrarAgendamento(nomePaciente, "Dr. Carlos", dataBase);
        service().confirmarAgendamento(novoAgendamento.getId(), "Sistema");
        definirUltimoAgendamento(novoAgendamento);
    }

    @Quando("a recepcionista registra um novo no-show para {string}")
    public void aRecepcionistaRegistraUmNovoNoShowPara(String nomePaciente) {
        service().definirDataHoraAtualParaTestes(ultimoAgendamento().getDataHora().plusMinutes(1));
        service().marcarNaoComparecimento(ultimoAgendamento().getId(), "Ana");
        definirUltimoAgendamento(service().obterAgendamento(ultimoAgendamento().getId()));
        sinalizacaoPaciente = service().consultarSinalizacaoPaciente(nomePaciente);
        service().limparDataHoraAtualParaTestes();
    }

    @Entao("o paciente deve ser sinalizado para acompanhamento")
    public void oPacienteDeveSerSinalizadoParaAcompanhamento() {
        assertNotNull(sinalizacaoPaciente);
    }

    @E("a sinalizacao deve informar {string}")
    public void aSinalizacaoDeveInformar(String mensagemEsperada) {
        assertEquals(mensagemEsperada, sinalizacaoPaciente);
    }

    @Quando("a recepcionista tenta confirmar novamente esse agendamento")
    public void aRecepcionistaTentaConfirmarNovamenteEsseAgendamento() {
        try {
            service().confirmarAgendamento(ultimoAgendamento().getId(), "Ana");
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @Entao("o sistema deve bloquear a confirmacao")
    public void oSistemaDeveBloquearAConfirmacao() {
        assertNotNull(excecaoCapturada, "A confirmacao deveria ter sido bloqueada");
    }
}
