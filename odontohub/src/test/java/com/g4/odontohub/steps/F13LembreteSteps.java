package com.g4.odontohub.steps;

import com.g4.odontohub.confirmacao.lembrete.application.LembreteApplicationService;
import com.g4.odontohub.confirmacao.lembrete.domain.model.StatusLembrete;
import io.cucumber.java.pt.*;

import static org.junit.jupiter.api.Assertions.*;

public class F13LembreteSteps {

    private final LembreteApplicationService service = new LembreteApplicationService();
    private Exception excecao;

    @Quando("a recepcionista gera um lembrete para o agendamento {int} do paciente {string} pelo canal {string}")
    public void geraLembrete(int agendamentoId, String paciente, String canal) {
        service.gerarLembrete((long) agendamentoId, paciente, canal);
    }

    @Dado("que existe um lembrete para o agendamento {int} do paciente {string} pelo canal {string}")
    public void existeLembrete(int agendamentoId, String paciente, String canal) {
        service.gerarLembrete((long) agendamentoId, paciente, canal);
    }

    @Dado("que existe um lembrete enviado para o agendamento {int} do paciente {string}")
    public void existeLembreteEnviado(int agendamentoId, String paciente) {
        service.gerarLembrete((long) agendamentoId, paciente, "SMS");
        service.enviar((long) agendamentoId);
    }

    @Quando("a recepcionista tenta gerar outro lembrete para o agendamento {int}")
    public void tentaGerarOutro(int agendamentoId) {
        tentar(() -> service.gerarLembrete((long) agendamentoId, "Paciente Teste", "SMS"));
    }

    @Quando("o sistema envia o lembrete do agendamento {int}")
    public void enviaLembrete(int agendamentoId) {
        service.enviar((long) agendamentoId);
    }

    @Quando("o paciente confirma a consulta do agendamento {int}")
    public void confirmaConsulta(int agendamentoId) {
        service.confirmar((long) agendamentoId);
    }

    @Quando("o paciente tenta confirmar a consulta do agendamento {int}")
    public void tentaConfirmar(int agendamentoId) {
        tentar(() -> service.confirmar((long) agendamentoId));
    }

    @Quando("o paciente recusa o lembrete do agendamento {int} com o motivo {string}")
    public void recusaLembrete(int agendamentoId, String motivo) {
        service.recusar((long) agendamentoId, motivo);
    }

    @Então("o lembrete do agendamento {int} deve estar com status {string}")
    public void lembreteComStatus(int agendamentoId, String status) {
        assertEquals(StatusLembrete.fromLabel(status), service.statusDoAgendamento((long) agendamentoId));
    }

    @Então("o sistema deve rejeitar a operação de lembrete")
    public void sistemaRejeitaLembrete() {
        assertNotNull(excecao, "O sistema deveria ter rejeitado a operação de lembrete");
    }

    private void tentar(Runnable acao) {
        try {
            acao.run();
        } catch (Exception e) {
            excecao = e;
            SharedTestServices.setLastException(e);
        }
    }
}
