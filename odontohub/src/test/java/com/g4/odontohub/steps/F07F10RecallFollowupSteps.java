package com.g4.odontohub.steps;

import com.g4.odontohub.relacionamentopaciente.followup.application.FollowupApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.application.RecallApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.StatusRecall;
import io.cucumber.java.pt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps compartilhados das funcionalidades F07 (Recall) e F10 (Follow-up).
 * Ambas dividem o gatilho "o dentista realizou o procedimento ... para ...",
 * por isso são automatizadas na mesma classe (glue global do Cucumber).
 */
public class F07F10RecallFollowupSteps {

    private final RecallApplicationService recallService = new RecallApplicationService();
    private final FollowupApplicationService followupService = new FollowupApplicationService();

    private String paciente;
    private String procedimento;
    private String tipoProcedimento;
    private String dentistaResponsavel;
    private Recall ultimoRecall;

    // ----- Gatilhos compartilhados -----

    @Dado("que o dentista realizou o procedimento {string} para {string}")
    public void dentistaRealizouProcedimento(String procedimento, String paciente) {
        this.procedimento = procedimento;
        this.tipoProcedimento = procedimento;
        this.paciente = paciente;
    }

    @Dado("que o dentista realizou um procedimento do tipo {string} para {string}")
    public void dentistaRealizouProcedimentoDoTipo(String tipo, String paciente) {
        this.tipoProcedimento = tipo;
        this.procedimento = tipo;
        this.paciente = paciente;
    }

    @Dado("que o dentista {string} é o responsável pelo atendimento")
    public void dentistaResponsavel(String nome) {
        this.dentistaResponsavel = nome;
    }

    // ----- F07 Recall -----

    @Quando("o sistema processa os gatilhos de recall")
    public void processaGatilhosRecall() {
        ultimoRecall = recallService.processarGatilhoRecall(paciente, procedimento);
    }

    @Então("{string} deve ser inserida na fila de recall com prazo de {int} dias")
    public void deveSerInseridaNaFilaComPrazo(String nome, int prazo) {
        Recall recall = recallService.buscarPorPaciente(nome);
        assertEquals(prazo, recall.getDiasParaRetorno());
        assertEquals(StatusRecall.NA_FILA, recall.getStatus());
    }

    @Então("o status do recall deve ser {string}")
    public void statusDoRecallDeveSer(String status) {
        Recall recall = recallService.buscarPorPaciente(paciente);
        assertEquals(StatusRecall.fromLabel(status), recall.getStatus());
    }

    @Dado("que {string} está na fila de recall com status {string}")
    public void estaNaFilaComStatus(String nome, String status) {
        this.paciente = nome;
        ultimoRecall = recallService.inserirNaFila(nome);
        assertEquals(StatusRecall.fromLabel(status), ultimoRecall.getStatus());
    }

    @Dado("que {string} possui um agendamento futuro confirmado")
    public void possuiAgendamentoFuturo(String nome) {
        recallService.registrarAgendamentoFuturo(nome, 777L);
    }

    @Quando("o sistema verifica sobreposição de agendamentos para recall")
    public void verificaSobreposicao() {
        recallService.verificarSobreposicao(paciente);
    }

    @Então("{string} deve ser removida da fila de recall")
    public void deveSerRemovidaDaFila(String nome) {
        assertFalse(recallService.estaNaFila(nome), nome + " não deveria estar na fila de recall");
    }

    @Então("o evento de cancelamento deve registrar o ID do agendamento existente")
    public void eventoCancelamentoRegistraId() {
        assertNotNull(recallService.getUltimoCancelamento(), "Evento de cancelamento não foi registrado");
        assertEquals(777L, recallService.getUltimoCancelamento().agendamentoExistenteId());
    }

    @Quando("a recepcionista agenda {string} diretamente da tela de Recall")
    public void agendaDiretamenteDaTelaDeRecall(String nome) {
        ultimoRecall = recallService.registrarConversao(nome, 888L);
    }

    @Então("o novo agendamento deve ser marcado com a flag de conversão de recall")
    public void novoAgendamentoComFlagConversao() {
        assertTrue(ultimoRecall.isFlagConversaoRecall(), "Agendamento deveria ter a flag de conversão de recall");
    }

    @Então("o status do recall deve ser atualizado para {string}")
    public void statusDoRecallAtualizadoPara(String status) {
        assertEquals(StatusRecall.fromLabel(status), ultimoRecall.getStatus());
    }

    // ----- F10 Follow-up -----

    @Quando("o sistema processa os gatilhos de pós-operatório")
    public void processaGatilhosPosOperatorio() {
        followupService.processarGatilhos(paciente, tipoProcedimento);
    }

    @Então("deve ser criada uma tarefa de ligação de 24h para {string}")
    public void deveSerCriadaTarefa24h(String nome) {
        assertTrue(followupService.existeTarefa(nome, "24h"), "Tarefa de 24h não foi criada");
    }

    @Então("deve ser criada uma tarefa de ligação de 72h para {string}")
    public void deveSerCriadaTarefa72h(String nome) {
        assertTrue(followupService.existeTarefa(nome, "72h"), "Tarefa de 72h não foi criada");
    }

    @Então("nenhuma tarefa de follow-up deve ser criada para {string}")
    public void nenhumaTarefaCriada(String nome) {
        assertTrue(followupService.tarefasDe(nome).isEmpty(), "Não deveria existir tarefa de follow-up");
    }

    @Dado("que existe uma tarefa de follow-up de 24h pendente para {string}")
    public void existeTarefa24hPendente(String nome) {
        this.paciente = nome;
        followupService.criarTarefa(nome, "24h");
    }

    @Dado("que existe uma tarefa de follow-up de 72h pendente para {string}")
    public void existeTarefa72hPendente(String nome) {
        this.paciente = nome;
        followupService.criarTarefa(nome, "72h");
    }

    @Quando("a recepcionista registra o checklist informando sangramento {string} e nível de dor {string}")
    public void registraChecklistSangramentoDor(String sangramento, String nivelDor) {
        followupService.registrarChecklist(paciente, ehSim(sangramento), Integer.parseInt(nivelDor),
                "", "Recepcionista", dentistaResponsavel);
    }

    @Quando("a recepcionista registra o checklist com nível de dor {string} e sangramento {string}")
    public void registraChecklistDorSangramento(String nivelDor, String sangramento) {
        followupService.registrarChecklist(paciente, ehSim(sangramento), Integer.parseInt(nivelDor),
                "", "Recepcionista", dentistaResponsavel);
    }

    @Então("o checklist de 24h deve ser salvo com os dados informados")
    public void checklist24hSalvo() {
        assertNotNull(followupService.buscarTarefa(paciente, "24h").getChecklist(), "Checklist não foi salvo");
    }

    @Então("a tarefa de ligação de 24h deve ser marcada como concluída")
    public void tarefa24hConcluida() {
        assertTrue(followupService.buscarTarefa(paciente, "24h").isConcluida(), "Tarefa de 24h deveria estar concluída");
    }

    @Então("o sistema deve acionar um alerta de emergência")
    public void sistemaAcionaAlertaEmergencia() {
        assertNotNull(followupService.getUltimaEmergencia(), "Alerta de emergência não foi acionado");
    }

    @Então("o dentista {string} deve ser notificado imediatamente")
    public void dentistaNotificado(String nome) {
        assertNotNull(followupService.getUltimaEmergencia());
        assertEquals(nome, followupService.getUltimaEmergencia().dentistaResponsavel());
    }

    @Então("o sistema deve acionar um alerta de emergência para o dentista responsável")
    public void alertaEmergenciaParaDentistaResponsavel() {
        assertNotNull(followupService.getUltimaEmergencia(), "Alerta de emergência não foi acionado");
    }

    private boolean ehSim(String valor) {
        return valor != null && valor.trim().equalsIgnoreCase("Sim");
    }
}
