package com.g4.odontohub.steps;

import com.g4.odontohub.relacionamentopaciente.followup.application.FollowupApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.application.RecallApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.*;
import io.cucumber.java.pt.*;

import java.time.LocalDate;
import java.util.List;

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
    private List<Recall> filaPriorizada;
    private List<Recall> slaVencido;

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

    // ----- F07 Recall: priorização, SLA e métricas -----

    @Dado("que foi disparado um recall de {string} para {string}")
    public void foiDisparadoRecall(String procedimento, String nome) {
        recallService.processarGatilhoRecall(nome, procedimento);
    }

    @Quando("a recepcionista consulta a fila priorizada de recall")
    public void consultaFilaPriorizada() {
        filaPriorizada = recallService.filaPriorizada();
    }

    @Então("{string} deve aparecer antes de {string} na fila priorizada")
    public void deveAparecerAntes(String primeiro, String segundo) {
        int idxPrimeiro = indiceNaFila(primeiro);
        int idxSegundo = indiceNaFila(segundo);
        assertTrue(idxPrimeiro >= 0 && idxSegundo >= 0, "Pacientes não encontrados na fila priorizada");
        assertTrue(idxPrimeiro < idxSegundo,
                primeiro + " deveria aparecer antes de " + segundo + " na fila priorizada");
    }

    @Então("o nível de prioridade de {string} deve ser {string}")
    public void nivelDePrioridade(String nome, String nivel) {
        assertEquals(NivelPrioridade.valueOf(nivel.trim().toUpperCase()), recallService.prioridadeDe(nome));
    }

    @Quando("a recepcionista registra {int} tentativas de contato sem sucesso para {string}")
    public void registraTentativasContato(int tentativas, String nome) {
        for (int i = 0; i < tentativas; i++) {
            recallService.registrarTentativaContato(nome);
        }
    }

    @Então("o recall de {string} deve ser escalonado")
    public void recallDeveSerEscalonado(String nome) {
        assertNotNull(recallService.getUltimoEscalonamento(), "O recall deveria ter sido escalonado");
        assertEquals(nome, recallService.getUltimoEscalonamento().paciente());
    }

    @Então("o escalonamento deve registrar {int} tentativas")
    public void escalonamentoRegistraTentativas(int tentativas) {
        assertEquals(tentativas, recallService.getUltimoEscalonamento().tentativas());
    }

    @Quando("{string} é convertida em agendamento a partir do recall")
    public void convertidaEmAgendamento(String nome) {
        recallService.registrarConversao(nome, 999L);
    }

    @Então("a taxa de conversão de recall deve ser {int}%")
    public void taxaDeConversao(int percentual) {
        assertEquals(percentual / 100.0, recallService.taxaConversao(), 0.001);
    }

    // ----- F07 Recall: priorização multifator, segmentação, contato detalhado, exclusão -----

    @Dado("que o recall de {string} possui fatores de risco com {int} meses sem retorno, inadimplente e risco clínico alto")
    public void recallComFatoresDeRisco(String nome, int mesesSemRetorno) {
        recallService.definirFatores(nome, new FatoresPriorizacao(
                mesesSemRetorno, 0, false, true, 0, 0, true, 0.0, false, false));
    }

    @Dado("que o recall de {string} possui fatores de inadimplência")
    public void recallComInadimplencia(String nome) {
        recallService.definirFatores(nome, new FatoresPriorizacao(
                0, 0, false, true, 0, 0, false, 0.0, false, false));
    }

    @Então("a pontuação de prioridade de {string} deve ser maior que a de {string}")
    public void pontuacaoMaiorQue(String maior, String menor) {
        assertTrue(recallService.pontuacaoDe(maior) > recallService.pontuacaoDe(menor),
                maior + " deveria ter pontuação maior que " + menor);
    }

    @Então("a categoria do recall de {string} deve ser {string}")
    public void categoriaDoRecall(String nome, String categoria) {
        assertEquals(CategoriaRecall.fromLabel(categoria), recallService.categoriaDe(nome));
    }

    @Então("o indicador visual de {string} deve ser {string}")
    public void indicadorVisualDe(String nome, String indicador) {
        assertEquals(IndicadorVisual.valueOf(indicador.trim().toUpperCase()), recallService.indicadorDe(nome));
    }

    @Quando("a recepcionista registra para {string} uma tentativa de contato por {string} com resultado {string} pela responsável {string}")
    public void registraTentativaDetalhada(String nome, String canal, String resultado, String responsavel) {
        recallService.registrarTentativaDetalhada(nome,
                CanalContato.fromLabel(canal), ResultadoContato.fromLabel(resultado), responsavel, 5, "contato de recall");
    }

    @Então("o histórico de contato de {string} deve conter {int} tentativa de contato")
    public void historicoDeContatoDeveConter(String nome, int quantidade) {
        assertEquals(quantidade, recallService.buscarPorPaciente(nome).getHistoricoTentativas().size());
    }

    @Então("o status do recall de {string} deve ser {string}")
    public void statusDoRecallDe(String nome, String status) {
        assertEquals(StatusRecall.fromLabel(status), recallService.buscarPorPaciente(nome).getStatus());
    }

    @Quando("a recepcionista consulta os recalls com SLA vencido daqui a {int} dias")
    public void consultaSlaVencido(int dias) {
        slaVencido = recallService.filaComSlaVencido(LocalDate.now().plusDays(dias));
    }

    @Então("{string} deve estar na lista de SLA vencido")
    public void deveEstarNoSlaVencido(String nome) {
        assertTrue(slaVencido.stream().anyMatch(r -> r.getPaciente().equals(nome)),
                nome + " deveria estar na lista de SLA vencido");
    }

    @Então("{string} não deve estar na lista de SLA vencido")
    public void naoDeveEstarNoSlaVencido(String nome) {
        assertTrue(slaVencido.stream().noneMatch(r -> r.getPaciente().equals(nome)),
                nome + " não deveria estar na lista de SLA vencido");
    }

    @Quando("o sistema exclui {string} do recall por motivo {string}")
    public void sistemaExcluiRecall(String nome, String motivo) {
        recallService.excluirAutomaticamente(nome, MotivoExclusao.fromLabel(motivo));
    }

    @Então("{string} não deve estar na fila priorizada")
    public void naoDeveEstarNaFilaPriorizada(String nome) {
        assertTrue(recallService.filaPriorizada().stream().noneMatch(r -> r.getPaciente().equals(nome)),
                nome + " não deveria estar na fila priorizada");
    }

    @Então("a métrica de pacientes recuperados deve ser {int}")
    public void metricaPacientesRecuperados(int esperado) {
        assertEquals(esperado, recallService.pacientesRecuperados());
    }

    @Então("a métrica de pacientes perdidos deve ser {int}")
    public void metricaPacientesPerdidos(int esperado) {
        assertEquals(esperado, recallService.pacientesPerdidos());
    }

    private int indiceNaFila(String nome) {
        for (int i = 0; i < filaPriorizada.size(); i++) {
            if (filaPriorizada.get(i).getPaciente().equals(nome)) {
                return i;
            }
        }
        return -1;
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
