package com.g4.odontohub.steps;

import com.g4.odontohub.agendamento.domain.model.StatusAgendamento;
import com.g4.odontohub.relacionamentopaciente.application.ChurnApplicationService;
import com.g4.odontohub.relacionamentopaciente.domain.model.AnaliseChurn;
import com.g4.odontohub.relacionamentopaciente.domain.model.StatusChurn;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class F11ChurnSteps {

    private ChurnApplicationService churnService;
    private Exception excecaoCapturada;
    private double receitaPerdidaCalculada;
    private int horasDeAgendaOciosa;
    private String pacienteDoUltimoAgendamento;

    @Dado("que o dentista possui valor médio por hora de {dinheiro}")
    public void queODentistaPossuiValorMedioPorHora(double valorMedioHora) {
        churnService = SharedTestServices.getChurnApplicationService();
        churnService.definirValorMedioHora(valorMedioHora);
    }

    @Dado("que o paciente {string} não possui agendamentos futuros")
    public void queOPacienteNaoPossuiAgendamentosFuturos(String nomePaciente) {
        churnService = SharedTestServices.getChurnApplicationService();
        churnService.definirSemAgendamentosFuturos(nomePaciente);
    }

    @Dado("que {string} não visita a clínica há {int} meses")
    @Dado("que o paciente {string} não visita a clínica há {int} meses")
    public void quePacienteNaoVisitaAClinicaHaMeses(String nomePaciente, Integer meses) {
        churnService = SharedTestServices.getChurnApplicationService();
        churnService.definirMesesSemRetorno(nomePaciente, meses);
    }

    @Dado("que {string} possui um Plano de Tratamento com status {string}")
    public void quePacientePossuiUmPlanoDeTratamentoComStatus(String nomePaciente, String status) {
        churnService = SharedTestServices.getChurnApplicationService();
        churnService.definirPlanoAtivo(nomePaciente, "Em Andamento".equalsIgnoreCase(status));
    }

    @Dado("que {string} possui um Plano de Tratamento ativo")
    public void quePacientePossuiUmPlanoDeTratamentoAtivo(String nomePaciente) {
        churnService = SharedTestServices.getChurnApplicationService();
        churnService.definirPlanoAtivo(nomePaciente, true);
    }

    @Dado("que {string} está classificado como {string}")
    @Dado("que o paciente {string} está classificado como {string}")
    public void quePacienteEstaClassificadoComo(String nomePaciente, String status) {
        churnService = SharedTestServices.getChurnApplicationService();
        churnService.definirStatusChurn(nomePaciente, mapearStatusChurn(status));
    }

    @Dado("que o dentista teve {int} horas de agenda cancelada no mês")
    public void queODentistaTeveHorasDeAgendaCanceladaNoMes(Integer horas) {
        churnService = SharedTestServices.getChurnApplicationService();
        horasDeAgendaOciosa = horas;
    }

    @Quando("o sistema recalcula o status de churn dos pacientes")
    public void oSistemaRecalculaOStatusDeChurnDosPacientes() {
        churnService.recalcularStatusChurn();
    }

    @Quando("{string} realiza um novo agendamento na clínica")
    public void pacienteRealizaUmNovoAgendamentoNaClinica(String nomePaciente) {
        churnService = SharedTestServices.getChurnApplicationService();
        churnService.registrarNovoAgendamento(nomePaciente);
    }

    @Quando("o sistema calcula a receita perdida por agenda ociosa")
    public void oSistemaCalculaAReceitaPerdidaPorAgendaOciosa() {
        churnService = SharedTestServices.getChurnApplicationService();
        receitaPerdidaCalculada = churnService.calcularReceitaPerdida(1L, horasDeAgendaOciosa);
    }

    @Quando("{string} não comparece à consulta sem aviso prévio")
    public void pacienteNaoCompareceAConsultaSemAvisoPrevio(String nomePaciente) {
        churnService = SharedTestServices.getChurnApplicationService();
        pacienteDoUltimoAgendamento = nomePaciente;
        churnService.registrarNaoComparecimento(nomePaciente);
    }

    @Quando("a recepcionista cancela o agendamento sem informar a categoria do motivo")
    public void aRecepcionistaCancelaOAgendamentoSemInformarACategoriaDoMotivo() {
        churnService = SharedTestServices.getChurnApplicationService();
        try {
            churnService.cancelarAgendamentoSemCategoria(pacienteDoUltimoAgendamento);
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }

    @Entao("{string} deve ser classificado como {string}")
    public void pacienteDeveSerClassificadoComo(String nomePaciente, String statusEsperado) {
        AnaliseChurn analiseChurn = churnService.buscarAnalisePorPaciente(nomePaciente);
        assertEquals(mapearStatusChurn(statusEsperado), analiseChurn.getStatusChurn());
    }

    @Entao("{string} deve ser classificada como {string}")
    public void pacienteDeveSerClassificadaComo(String nomePaciente, String statusEsperado) {
        pacienteDeveSerClassificadoComo(nomePaciente, statusEsperado);
    }

    @E("deve ser gerado um alerta de inatividade progressiva para {string}")
    public void deveSerGeradoUmAlertaDeInatividadeProgressivaPara(String nomePaciente) {
        assertNotNull(churnService.getUltimoAlertaInatividade());
        assertEquals(churnService.buscarAnalisePorPaciente(nomePaciente).getPacienteId().getPacienteId(),
                churnService.getUltimoAlertaInatividade().pacienteId());
    }

    @Entao("o valor de receita perdida deve ser {dinheiro}")
    public void oValorDeReceitaPerdidaDeveSer(double valorEsperado) {
        assertEquals(valorEsperado, receitaPerdidaCalculada, 0.001);
    }

    @Entao("o agendamento deve ser registrado como {string}")
    public void oAgendamentoDeveSerRegistradoComo(String statusEsperado) {
        assertNotNull(churnService.getUltimoAgendamento());
        assertEquals(mapearStatusAgendamento(statusEsperado), churnService.getUltimoAgendamento().getStatus());
    }

    @E("este registro deve ser contabilizado separadamente dos cancelamentos antecipados no dashboard")
    public void esteRegistroDeveSerContabilizadoSeparadamenteDosCancelamentosAntecipadosNoDashboard() {
        assertEquals(1L, churnService.contarNaoComparecimentos());
        assertEquals(0L, churnService.contarCancelamentosAntecipados());
    }

    @Entao("o sistema deve bloquear o cancelamento")
    public void oSistemaDeveBloquearOCancelamento() {
        assertNotNull(excecaoCapturada, "O cancelamento deveria ter sido bloqueado");
        assertTrue(excecaoCapturada instanceof IllegalArgumentException);
    }

    @E("a mensagem deve informar {string}")
    public void aMensagemDeveInformar(String mensagemEsperada) {
        assertNotNull(churnService.getUltimaMensagemErro());
        assertTrue(churnService.getUltimaMensagemErro().contains(mensagemEsperada));
    }

    private StatusChurn mapearStatusChurn(String status) {
        return switch (status.toLowerCase()) {
            case "ativo" -> StatusChurn.ATIVO;
            case "zona de risco" -> StatusChurn.ZONA_DE_RISCO;
            case "evadido" -> StatusChurn.EVADIDO;
            case "reativado" -> StatusChurn.REATIVADO;
            default -> throw new IllegalArgumentException("Status de churn desconhecido: " + status);
        };
    }

    private StatusAgendamento mapearStatusAgendamento(String status) {
        return switch (status.toLowerCase()) {
            case "não compareceu", "nao compareceu" -> StatusAgendamento.NAO_COMPARECEU;
            case "cancelado" -> StatusAgendamento.CANCELADO;
            case "confirmado" -> StatusAgendamento.CONFIRMADO;
            default -> throw new IllegalArgumentException("Status de agendamento desconhecido: " + status);
        };
    }
}
