package com.g4.odontohub.steps;

import com.g4.odontohub.agendamento.application.AgendamentoService;
import com.g4.odontohub.agendamento.domain.Agendamento;
import com.g4.odontohub.agendamento.domain.StatusAgendamento;
import com.g4.odontohub.agendamento.domain.TipoAgendamento;
import com.g4.odontohub.infra.InMemoryAgendamentoRepository;
import com.g4.odontohub.shared.exception.DomainException;
import io.cucumber.java.pt.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para F01 - Agendamento de Consultas e Retornos.
 * Chama exclusivamente o AgendamentoService (camada application).
 * Nunca acede a repositórios JPA ou banco de dados.
 */
public class AgendamentoSteps {

    private final InMemoryAgendamentoRepository repo = new InMemoryAgendamentoRepository();
    private final AgendamentoService service = new AgendamentoService(repo);

    private Agendamento resultado;
    private boolean possuiPlanoAtivo = false;
    private boolean pacienteInadimplente = false;
    private boolean autorizadoPorDentista = false;

    // ── Pré-condições ────────────────────────────────────────────────────────

    @Dado("que o paciente {string} não possui plano de tratamento ativo")
    public void pacienteSemPlano(String nome) {
        possuiPlanoAtivo = false;
    }

    @Dado("que o paciente {string} possui um plano de tratamento ativo")
    public void pacienteComPlano(String nome) {
        possuiPlanoAtivo = true;
    }

    @Dado("que já existe um agendamento para o dentista {string} no dia {string} às {string}")
    public void agendamentoPreExistente(String dentista, String data, String hora) {
        service.registrar(1L, LocalDateTime.parse(data + "T" + hora + ":00"),
                false, false, false, "sistema");
    }

    @Dado("que o paciente {string} possui uma parcela com status {string}")
    public void parcelaComStatus(String nome, String status) {
        pacienteInadimplente = "INADIMPLENTE".equals(status);
    }

    @Dado("que o dentista {string} autorizou o agendamento")
    public void dentistaAutorizou(String dentista) {
        autorizadoPorDentista = true;
    }

    @Dado("que existe um agendamento com status {string} para {string}")
    public void agendamentoExistenteComStatus(String status, String paciente) {
        resultado = service.registrar(1L, LocalDateTime.now().plusDays(5),
                false, false, false, "recepcionista");
        if ("CONFIRMADO".equals(status)) {
            resultado = service.confirmar(resultado.getId(), "recepcionista");
        }
    }

    // ── Ações ────────────────────────────────────────────────────────────────

    @Quando("a recepcionista registra um agendamento para {string} no dia {string} às {string}")
    public void registrarAgendamento(String paciente, String data, String hora) {
        try {
            resultado = service.registrar(1L,
                    LocalDateTime.parse(data + "T" + hora + ":00"),
                    possuiPlanoAtivo, pacienteInadimplente, autorizadoPorDentista,
                    "recepcionista");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a recepcionista tenta registrar outro agendamento para {string} no dia {string} às {string}")
    public void tentarRegistrarConflito(String dentista, String data, String hora) {
        try {
            service.registrar(1L, LocalDateTime.parse(data + "T" + hora + ":00"),
                    false, false, false, "recepcionista");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a recepcionista tenta registrar um agendamento para {string}")
    public void tentarRegistrarParaInadimplente(String paciente) {
        try {
            service.registrar(1L, LocalDateTime.now().plusDays(1),
                    false, pacienteInadimplente, autorizadoPorDentista, "recepcionista");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a recepcionista registra um agendamento para {string} com autorização do dentista")
    public void registrarComAutorizacao(String paciente) {
        try {
            resultado = service.registrar(1L, LocalDateTime.now().plusDays(1),
                    false, true, true, "recepcionista");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a recepcionista tenta registrar um agendamento para o dia {string}")
    public void tentarRegistrarDataPassada(String data) {
        try {
            service.registrar(1L, LocalDateTime.parse(data + "T09:00:00"),
                    false, false, false, "recepcionista");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a recepcionista confirma o agendamento")
    public void confirmarAgendamento() {
        resultado = service.confirmar(resultado.getId(), "recepcionista");
    }

    @Quando("a recepcionista cancela o agendamento com o motivo {string}")
    public void cancelarAgendamento(String motivo) {
        resultado = service.cancelar(resultado.getId(), motivo, "recepcionista");
    }

    // ── Verificações ─────────────────────────────────────────────────────────

    @Então("o agendamento deve ser criado com status {string}")
    public void verificarStatus(String status) {
        assertNull(ScenarioContext.get().excecao,
                () -> "Erro inesperado: " + ScenarioContext.get().excecao.getMessage());
        assertEquals(StatusAgendamento.valueOf(status), resultado.getStatus());
    }

    @Então("o tipo do agendamento deve ser classificado automaticamente como {string}")
    public void verificarTipo(String tipo) {
        assertEquals(TipoAgendamento.valueOf(tipo), resultado.getTipo());
    }

    @Então("o sistema deve rejeitar o agendamento")
    public void sistemaRejeitaAgendamento() {
        assertNotNull(ScenarioContext.get().excecao);
    }

    @Então("o sistema deve bloquear o registro do agendamento")
    public void sistemaBloqueia() {
        assertNotNull(ScenarioContext.get().excecao);
    }

    @Então("o campo {string} deve ser verdadeiro")
    public void campoVerdadeiro(String campo) {
        if ("autorizadoPorDentista".equals(campo)) {
            assertTrue(resultado.isAutorizadoPorDentista());
        }
    }

    @Então("o status do agendamento deve ser alterado para {string}")
    public void statusAlterado(String status) {
        assertEquals(StatusAgendamento.valueOf(status), resultado.getStatus());
    }

    @Então("o histórico de status deve registrar a alteração com data e responsável")
    public void historicoRegistrado() {
        assertFalse(resultado.getHistoricoStatus().isEmpty());
        assertNotNull(resultado.getHistoricoStatus()
                .get(resultado.getHistoricoStatus().size() - 1).getResponsavel());
    }

    @Então("o histórico de status deve registrar o motivo {string}")
    public void historicoComMotivo(String motivo) {
        assertTrue(resultado.getHistoricoStatus().stream()
                .anyMatch(h -> motivo.equals(h.getMotivo())));
    }
}
