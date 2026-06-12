package com.g4.odontohub.steps;

import com.g4.odontohub.relacionamentopaciente.churn.application.ChurnApplicationService;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.StatusChurn;
import io.cucumber.java.pt.*;

import static org.junit.jupiter.api.Assertions.*;

public class F11ChurnSteps {

    private final ChurnApplicationService churnService = new ChurnApplicationService();
    private double receitaPerdida;
    private int horasCanceladas;
    private Exception excecaoCapturada;

    @Dado("que o dentista possui valor médio por hora de {dinheiro}")
    public void valorMedioPorHora(double valor) {
        churnService.definirValorMedioHora(valor);
    }

    @Dado("que o paciente {string} não possui agendamentos futuros")
    public void semAgendamentosFuturos(String nome) {
        churnService.definirSemAgendamentoFuturo(nome);
    }

    @Dado("que {string} não visita a clínica há {int} meses")
    public void naoVisitaHaMeses(String nome, int meses) {
        churnService.definirMesesSemRetorno(nome, meses);
    }

    @Dado("que o paciente {string} não visita a clínica há {int} meses")
    public void pacienteNaoVisitaHaMeses(String nome, int meses) {
        churnService.definirMesesSemRetorno(nome, meses);
    }

    @Dado("que {string} possui um Plano de Tratamento com status {string}")
    public void possuiPlanoComStatus(String nome, String status) {
        churnService.definirPlanoAtivo(nome, planoEstaAtivo(status));
    }

    @Dado("que {string} possui um Plano de Tratamento ativo")
    public void possuiPlanoAtivo(String nome) {
        churnService.definirPlanoAtivo(nome, true);
    }

    @Quando("o sistema recalcula o status de churn dos pacientes")
    public void recalculaChurn() {
        churnService.recalcular();
    }

    @Então("{string} deve ser classificado como {string}")
    public void deveSerClassificadoComo(String nome, String status) {
        assertEquals(StatusChurn.fromLabel(status), churnService.statusDe(nome));
    }

    @Então("{string} deve ser classificada como {string}")
    public void deveSerClassificadaComo(String nome, String status) {
        assertEquals(StatusChurn.fromLabel(status), churnService.statusDe(nome));
    }

    @E("deve ser gerado um alerta de inatividade progressiva para {string}")
    public void alertaDeInatividade(String nome) {
        assertTrue(churnService.temAlertaInatividade(nome),
                "Deveria ter alerta de inatividade progressiva para " + nome);
    }

    @Dado("que o paciente {string} está classificado como {string}")
    public void pacienteClassificadoComo(String nome, String status) {
        churnService.classificarManualmente(nome, status);
    }

    @Quando("{string} realiza um novo agendamento na clínica")
    public void realizaNovoAgendamento(String nome) {
        churnService.reativar(nome);
    }

    @Então("o status de churn de {string} deve ser atualizado para {string}")
    public void statusDeChurnAtualizado(String nome, String status) {
        assertEquals(StatusChurn.fromLabel(status), churnService.statusDe(nome));
    }

    @Dado("que o dentista teve {int} horas de agenda cancelada no mês")
    public void horasDeAgendaCancelada(int horas) {
        this.horasCanceladas = horas;
    }

    @Quando("o sistema calcula a receita perdida por agenda ociosa")
    public void calculaReceitaPerdida() {
        receitaPerdida = churnService.calcularReceitaPerdida(horasCanceladas);
    }

    @Então("o valor de receita perdida deve ser {dinheiro}")
    public void valorReceitaPerdida(double valor) {
        assertEquals(valor, receitaPerdida, 0.001);
    }

    @Quando("{string} não comparece à consulta sem aviso prévio")
    public void naoCompareceSemAviso(String nome) {
        churnService.registrarNoShow(nome);
    }

    @Então("o agendamento deve ser registrado como {string}")
    public void agendamentoRegistradoComo(String label) {
        assertEquals(label, churnService.getUltimoRegistroAgendamento());
    }

    @E("este registro deve ser contabilizado separadamente dos cancelamentos antecipados no dashboard")
    public void contabilizadoSeparadamente() {
        assertEquals(1, churnService.getNoShowCount());
        assertEquals(0, churnService.getCancelamentoCount());
    }

    @Quando("a recepcionista cancela o agendamento sem informar a categoria do motivo")
    public void cancelaSemCategoria() {
        try {
            churnService.cancelarAgendamento("", "");
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o sistema deve bloquear o cancelamento")
    public void sistemaBloqueiaCancelamento() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter bloqueado o cancelamento");
    }

    @E("a mensagem deve informar {string}")
    public void mensagemDeveInformar(String mensagem) {
        assertNotNull(excecaoCapturada);
        assertEquals(mensagem, excecaoCapturada.getMessage());
    }

    private boolean planoEstaAtivo(String status) {
        String s = status.trim().toUpperCase();
        return s.equals("PENDENTE") || s.equals("EM ANDAMENTO");
    }
}
