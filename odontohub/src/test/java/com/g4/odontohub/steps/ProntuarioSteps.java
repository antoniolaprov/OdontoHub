package com.g4.odontohub.steps;

import com.g4.odontohub.infra.InMemoryProntuarioRepository;
import com.g4.odontohub.prontuario.application.ProntuarioService;
import com.g4.odontohub.prontuario.domain.Prontuario;
import com.g4.odontohub.prontuario.domain.StatusProntuario;
import com.g4.odontohub.shared.exception.DomainException;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.*;

public class ProntuarioSteps {

    private final InMemoryProntuarioRepository repo = new InMemoryProntuarioRepository();
    private final ProntuarioService service = new ProntuarioService(repo);

    private boolean agendamentoConfirmado = false;
    private Prontuario prontuarioResultado;
    private Prontuario prontuarioAnterior;
    private Long fichaIdAtual;

    // ── Contexto ─────────────────────────────────────────────────────────────

    @Dado("que existe um agendamento confirmado para {string}")
    public void agendamentoConfirmadoSetup(String paciente) {
        agendamentoConfirmado = true;
    }

    // ── Pré-condições ────────────────────────────────────────────────────────

    @Dado("que o paciente {string} não possui prontuário")
    public void pacienteSemProntuario(String paciente) {
        // repo já está vazio — no-op
    }

    @Dado("que o paciente {string} já possui um prontuário ativo")
    public void pacienteJaPossuiProntuario(String paciente) {
        prontuarioAnterior = service.registrarFichaClinica(1L, true, "Primeira consulta");
        prontuarioResultado = prontuarioAnterior;
    }

    @Dado("que existe uma ficha clínica confirmada para {string} com a evolução {string}")
    public void fichaClinicaConfirmada(String paciente, String evolucao) {
        prontuarioResultado = service.registrarFichaClinica(1L, true, evolucao);
        fichaIdAtual = ultimaFichaId(prontuarioResultado);
        prontuarioResultado = service.confirmarFicha(prontuarioResultado.getId(), fichaIdAtual);
    }

    @Dado("que não existe agendamento confirmado para {string} na data atual")
    public void semAgendamentoConfirmado(String paciente) {
        agendamentoConfirmado = false;
    }

    @Dado("que o prontuário de {string} tem status {string}")
    public void prontuarioComStatus(String paciente, String status) {
        prontuarioResultado = service.registrarFichaClinica(1L, true, "Atendimento inicial");
        assertEquals(StatusProntuario.valueOf(status), prontuarioResultado.getStatus());
    }

    @Dado("que existe um prontuário para {string}")
    public void prontuarioExistente(String paciente) {
        prontuarioResultado = service.registrarFichaClinica(1L, true, "Atendimento");
    }

    // ── Ações ────────────────────────────────────────────────────────────────

    @Quando("o dentista registra a primeira ficha clínica para {string}")
    public void registrarPrimeiraFicha(String paciente) {
        try {
            prontuarioResultado = service.registrarFichaClinica(1L, agendamentoConfirmado, "Primeira consulta");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista registra um novo atendimento para {string}")
    public void registrarNovoAtendimento(String paciente) {
        try {
            prontuarioResultado = service.registrarFichaClinica(1L, agendamentoConfirmado, "Segundo atendimento");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista tenta editar a evolução {string}")
    public void tentarEditarEvolucao(String evolucao) {
        try {
            service.editarEvolucao(prontuarioResultado.getId(), fichaIdAtual, "Nova evolução");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista tenta registrar uma ficha clínica para {string}")
    public void tentarRegistrarFicha(String paciente) {
        try {
            service.registrarFichaClinica(1L, agendamentoConfirmado, "Atendimento");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista encerra o prontuário com a justificativa {string}")
    public void encerrarProntuario(String justificativa) {
        prontuarioResultado = service.encerrar(prontuarioResultado.getId(), justificativa);
    }

    @Quando("qualquer usuário tenta excluir o prontuário de {string}")
    public void tentarExcluirProntuario(String paciente) {
        try {
            service.excluir(prontuarioResultado.getId());
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    // ── Verificações ─────────────────────────────────────────────────────────

    @Então("o sistema deve criar automaticamente um prontuário para {string}")
    public void deveCriarProntuarioAutomatico(String paciente) {
        assertNotNull(prontuarioResultado);
        assertNotNull(prontuarioResultado.getId());
    }

    @Então("a ficha clínica deve ser vinculada ao prontuário criado")
    public void fichaDeveEstarVinculada() {
        assertFalse(prontuarioResultado.getFichas().isEmpty());
    }

    @Então("o prontuário deve ter status {string}")
    public void prontuarioDeveTermStatus(String status) {
        assertEquals(StatusProntuario.valueOf(status), prontuarioResultado.getStatus());
    }

    @Então("o sistema deve criar uma nova ficha clínica dentro do prontuário existente")
    public void deveCriarNovaFichaNoMesmoProntuario() {
        assertEquals(2, prontuarioResultado.getFichas().size());
    }

    @Então("não deve criar um novo prontuário")
    public void naoDeveCriarNovoProntuario() {
        assertEquals(prontuarioAnterior.getId(), prontuarioResultado.getId());
    }

    @Então("o status do prontuário deve ser alterado para {string}")
    public void statusProntuarioAlterado(String status) {
        assertEquals(StatusProntuario.valueOf(status), prontuarioResultado.getStatus());
    }

    @Então("a justificativa deve ser registrada")
    public void justificativaDeveSerRegistrada() {
        assertNotNull(prontuarioResultado.getJustificativaEncerramento());
        assertFalse(prontuarioResultado.getJustificativaEncerramento().isEmpty());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long ultimaFichaId(Prontuario p) {
        return p.getFichas().get(p.getFichas().size() - 1).getId();
    }
}
