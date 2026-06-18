package com.g4.odontohub.steps;

import com.g4.odontohub.confirmacao.naocomparecimento.application.NaoComparecimentoApplicationService;
import io.cucumber.java.pt.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class F17NaoComparecimentoSteps {

    private final NaoComparecimentoApplicationService service = new NaoComparecimentoApplicationService();
    private Exception excecao;

    @Quando("a recepcionista registra uma falta injustificada do paciente {string} no agendamento {int} ocorrida ontem")
    public void registraFaltaInjustificada(String paciente, int agendamentoId) {
        service.registrarFalta((long) agendamentoId, paciente, LocalDate.now().minusDays(1), false, null);
    }

    @Quando("a recepcionista tenta registrar uma falta do paciente {string} no agendamento {int} com data futura")
    public void tentaRegistrarFaltaFutura(String paciente, int agendamentoId) {
        tentar(() -> service.registrarFalta((long) agendamentoId, paciente, LocalDate.now().plusDays(5), false, null));
    }

    @Dado("que o paciente {string} teve uma falta justificada no agendamento {int}")
    public void faltaJustificada(String paciente, int agendamentoId) {
        service.registrarFalta((long) agendamentoId, paciente, LocalDate.now().minusDays(1), true, "Atestado médico");
    }

    @Dado("que o paciente {string} teve uma falta injustificada no agendamento {int}")
    public void faltaInjustificada(String paciente, int agendamentoId) {
        service.registrarFalta((long) agendamentoId, paciente, LocalDate.now().minusDays(1), false, null);
    }

    @Dado("que o paciente {string} acumulou {int} faltas injustificadas")
    public void acumulouFaltas(String paciente, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            service.registrarFalta((long) (900 + i), paciente, LocalDate.now().minusDays(1), false, null);
        }
    }

    @Quando("a recepcionista tenta registrar outra falta no agendamento {int}")
    public void tentaRegistrarOutraFalta(int agendamentoId) {
        tentar(() -> service.registrarFalta((long) agendamentoId, "Paciente Teste",
                LocalDate.now().minusDays(1), false, null));
    }

    @Então("o paciente {string} deve ter {int} falta registrada")
    public void deveTerFaltas(String paciente, int quantidade) {
        assertEquals(quantidade, service.totalFaltasDe(paciente));
    }

    @Então("o paciente {string} deve ser reincidente")
    public void deveSerReincidente(String paciente) {
        assertTrue(service.pacienteReincidente(paciente), paciente + " deveria ser reincidente");
    }

    @Então("o paciente {string} não deve ser reincidente")
    public void naoDeveSerReincidente(String paciente) {
        assertFalse(service.pacienteReincidente(paciente), paciente + " não deveria ser reincidente");
    }

    @Então("o sistema deve rejeitar a operação de falta")
    public void sistemaRejeitaFalta() {
        assertNotNull(excecao, "O sistema deveria ter rejeitado a operação de falta");
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
