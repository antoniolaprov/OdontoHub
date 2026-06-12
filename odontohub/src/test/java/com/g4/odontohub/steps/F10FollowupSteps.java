package com.g4.odontohub.steps;

import com.g4.odontohub.relacionamentopaciente.application.FollowupApplicationService;
import com.g4.odontohub.relacionamentopaciente.domain.model.ChecklistFollowup;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class F10FollowupSteps {

    private final FollowupApplicationService followupService = SharedTestServices.getFollowupApplicationService();
    private String tipoLigacaoEmAndamento;

    @Dado("que o dentista {string} é o responsável pelo atendimento")
    public void queODentistaEResponsavelPeloAtendimento(String nomeDentista) {
        followupService.cadastrarDentista(nomeDentista);
    }

    @Dado("que o dentista realizou um procedimento do tipo {string} para {string}")
    public void queODentistaRealizouUmProcedimentoDoTipoPara(String tipoProcedimento, String nomePaciente) {
        followupService.registrarProcedimentoRealizado(nomePaciente, "Dr. Carlos", tipoProcedimento);
    }

    @Dado("que o dentista realizou o procedimento {string} para {string}")
    public void queODentistaRealizouOProcedimentoPara(String tipoProcedimento, String nomePaciente) {
        followupService.registrarProcedimentoRealizado(nomePaciente, "Dr. Carlos", tipoProcedimento);
    }

    @Dado("que existe uma tarefa de follow-up de 24h pendente para {string}")
    public void queExisteUmaTarefaDeFollowupDe24hPendentePara(String nomePaciente) {
        garantirFollowupCirurgico(nomePaciente);
        tipoLigacaoEmAndamento = "24h";
        assertTrue(followupService.existeTarefa24hPendente(nomePaciente));
    }

    @Dado("que existe uma tarefa de follow-up de 72h pendente para {string}")
    public void queExisteUmaTarefaDeFollowupDe72hPendentePara(String nomePaciente) {
        garantirFollowupCirurgico(nomePaciente);
        tipoLigacaoEmAndamento = "72h";
        assertTrue(followupService.existeTarefa72hPendente(nomePaciente));
    }

    @Quando("o sistema processa os gatilhos de pós-operatório")
    public void oSistemaProcessaOsGatilhosDePosOperatorio() {
        followupService.processarGatilhosPosOperatorio();
    }

    @Quando("a recepcionista registra o checklist informando sangramento {string} e nível de dor {string}")
    public void aRecepcionistaRegistraOChecklistInformandoSangramentoENivelDeDor(String sangramento, String nivelDor) {
        registrarChecklistConformeLigacaoEmAndamento(sangramento, nivelDor);
    }

    @Quando("a recepcionista registra o checklist com nível de dor {string} e sangramento {string}")
    public void aRecepcionistaRegistraOChecklistComNivelDeDorESangramento(String nivelDor, String sangramento) {
        registrarChecklistConformeLigacaoEmAndamento(sangramento, nivelDor);
    }

    @Entao("deve ser criada uma tarefa de ligação de 24h para {string}")
    public void deveSerCriadaUmaTarefaDeLigacaoDe24hPara(String nomePaciente) {
        assertTrue(followupService.existeTarefa24hPendente(nomePaciente));
    }

    @E("deve ser criada uma tarefa de ligação de 72h para {string}")
    public void deveSerCriadaUmaTarefaDeLigacaoDe72hPara(String nomePaciente) {
        assertTrue(followupService.existeTarefa72hPendente(nomePaciente));
    }

    @Entao("o checklist de 24h deve ser salvo com os dados informados")
    public void oChecklistDe24hDeveSerSalvoComOsDadosInformados() {
        ChecklistFollowup checklist = followupService.getChecklist24h("Gabriela Souza");
        assertNotNull(checklist);
        assertFalse(checklist.sangramentoAtivo());
        assertEquals(2, checklist.nivelDor());
    }

    @E("a tarefa de ligação de 24h deve ser marcada como concluída")
    public void aTarefaDeLigacaoDe24hDeveSerMarcadaComoConcluida() {
        assertFalse(followupService.existeTarefa24hPendente("Gabriela Souza"));
    }

    @Entao("o sistema deve acionar um alerta de emergência")
    public void oSistemaDeveAcionarUmAlertaDeEmergencia() {
        assertNotNull(followupService.getUltimaEmergenciaIdentificada());
    }

    @E("o dentista {string} deve ser notificado imediatamente")
    public void oDentistaDeveSerNotificadoImediatamente(String nomeDentista) {
        assertEquals(nomeDentista, followupService.getNomeDentistaNotificado());
    }

    @Entao("o sistema deve acionar um alerta de emergência para o dentista responsável")
    public void oSistemaDeveAcionarUmAlertaDeEmergenciaParaODentistaResponsavel() {
        assertNotNull(followupService.getUltimaEmergenciaIdentificada());
        assertEquals("Dr. Carlos", followupService.getNomeDentistaNotificado());
    }

    @Entao("nenhuma tarefa de follow-up deve ser criada para {string}")
    public void nenhumaTarefaDeFollowupDeveSerCriadaPara(String nomePaciente) {
        assertFalse(followupService.existeFollowupParaPaciente(nomePaciente));
    }

    private void garantirFollowupCirurgico(String nomePaciente) {
        if (!followupService.existeFollowupParaPaciente(nomePaciente)) {
            followupService.registrarProcedimentoRealizado(nomePaciente, "Dr. Carlos", "Cirurgia");
            followupService.processarGatilhosPosOperatorio();
        }
    }

    private void registrarChecklistConformeLigacaoEmAndamento(String sangramento, String nivelDor) {
        boolean haSangramento = mapearSangramento(sangramento);
        int dor = Integer.parseInt(nivelDor);

        if ("72h".equals(tipoLigacaoEmAndamento)) {
            followupService.registrarChecklist72h("Gabriela Souza", haSangramento, dor, "", "Recepcionista");
            return;
        }

        followupService.registrarChecklist24h("Gabriela Souza", haSangramento, dor, "", "Recepcionista");
    }

    private boolean mapearSangramento(String sangramento) {
        return "sim".equalsIgnoreCase(sangramento);
    }
}
