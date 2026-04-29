package com.g4.odontohub.steps;

import com.g4.odontohub.recall.application.RecallApplicationService;
import com.g4.odontohub.recall.domain.model.Agendamento;
import com.g4.odontohub.recall.domain.model.FilaDeRecall;
import com.g4.odontohub.recall.domain.model.StatusRecall;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.*;

public class F07RecallSteps {

    private final RecallApplicationService service = new RecallApplicationService();
    private Agendamento ultimoAgendamento;

   @Dado("que o paciente {string} está cadastrado no sistema")
    public void pacienteCadastrado(String nome) {
        service.cadastrarPaciente(nome);
    }

    @Dado("que o dentista realizou o procedimento {string} para {string}")
    public void dentistaRealizouProcedimento(String procedimento, String nomePaciente) {
        service.registrarProcedimento(nomePaciente, procedimento);
    }

    @Quando("o sistema processa os gatilhos de recall")
    public void sistemaProcessaGatilhosDeRecall() {
        service.processarGatilhosDeRecall();
    }

    @Então("{string} deve ser inserida na fila de recall com prazo de {int} dias")
    public void pacienteInseridoNaFilaComPrazo(String nomePaciente, int prazo) {
        FilaDeRecall fila = service.getFilaDeRecall(nomePaciente);
        assertNotNull(fila, "Paciente não encontrado na fila de recall");
        assertEquals(prazo, fila.getPrazoEmDias());
    }

    @E("o status do recall deve ser {string}")
    public void statusDoRecallDeverSer(String statusEsperado) {
        // O cenário contextualiza o paciente pelo último acesso; usamos "Ana Ferreira"
        FilaDeRecall fila = service.getFilaDeRecall("Ana Ferreira");
        assertNotNull(fila);
        StatusRecall esperado = StatusRecall.valueOf(
            statusEsperado.toUpperCase().replace(" ", "_").replace("Ã", "A")
        );
        assertEquals(esperado, fila.getStatus());
    }

    @Dado("que {string} está na fila de recall com status {string}")
    public void pacienteNaFilaComStatus(String nomePaciente, String status) {
        service.adicionarNaFilaComStatus(nomePaciente, status);
    }

    @E("que {string} possui um agendamento futuro confirmado")
    public void pacientePossuiAgendamentoFuturo(String nomePaciente) {
        service.registrarAgendamentoFuturo(nomePaciente);
    }

    @Quando("o sistema verifica sobreposição de agendamentos para recall")
    public void sistemaVerificaSobreposicao() {
        service.verificarSobreposicaoParaRecall("Ana Ferreira");
    }

    @Então("{string} deve ser removida da fila de recall")
    public void pacienteRemovidoDaFila(String nomePaciente) {
        FilaDeRecall fila = service.getFilaDeRecall(nomePaciente);
        assertNotNull(fila);
        assertEquals(StatusRecall.CANCELADO, fila.getStatus());
    }

    @E("o evento de cancelamento deve registrar o ID do agendamento existente")
    public void eventoDecancelamentoRegistraId() {
        assertNotNull(service.getUltimoEventoCancelado());
        assertNotNull(service.getUltimoEventoCancelado().getIdAgendamentoExistente());
        assertFalse(service.getUltimoEventoCancelado().getIdAgendamentoExistente().isBlank());
    }

    @Quando("a recepcionista agenda {string} diretamente da tela de Recall")
    public void recepcionistaAgendaViaRecall(String nomePaciente) {
        ultimoAgendamento = service.agendarViaRecall(nomePaciente);
    }

    @Então("o novo agendamento deve ser marcado com a flag de conversão de recall")
    public void agendamentoMarcadoComFlagConversao() {
        assertNotNull(ultimoAgendamento);
        assertTrue(ultimoAgendamento.isFlagConversaoRecall());
    }

    @E("o status do recall deve ser atualizado para {string}")
    public void statusDoRecallAtualizadoPara(String statusEsperado) {
        FilaDeRecall fila = service.getFilaDeRecall("Ana Ferreira");
        assertNotNull(fila);
        StatusRecall esperado = StatusRecall.valueOf(
            statusEsperado.toUpperCase().replace(" ", "_").replace("Ã", "A")
        );
        assertEquals(esperado, fila.getStatus());
    }
}