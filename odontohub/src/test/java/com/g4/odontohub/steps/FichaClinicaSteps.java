package com.g4.odontohub.steps;

import com.g4.odontohub.fichaClinica.application.FichaClinicaService;
import com.g4.odontohub.infra.InMemoryAgendamentoRepository;
import com.g4.odontohub.infra.InMemoryProntuarioRepository;
import com.g4.odontohub.prontuario.domain.*;
import com.g4.odontohub.agendamento.domain.*;
import io.cucumber.java.pt.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FichaClinicaSteps {

    private final ScenarioContext context = ScenarioContext.get();

    private FichaClinicaService service;
    private ProntuarioRepository prontuarioRepository;
    private AgendamentoRepository agendamentoRepository;

    private Long pacienteId = 1L;
    private Long prontuarioId = 1L;
    private Long fichaId = 1L;
    private Long agendamentoId = 1L;

    public FichaClinicaSteps() {
        this.prontuarioRepository = new InMemoryProntuarioRepository();
        this.agendamentoRepository = new InMemoryAgendamentoRepository();
        this.service = new FichaClinicaService(prontuarioRepository, agendamentoRepository);
    }

    // =========================================================
    // CONTEXTO
    // =========================================================

    @Dado("que o paciente {string} está cadastrado no sistema")
    public void pacienteCadastrado(String nome) {
        // Não precisa fazer nada — só garantir ID fixo
    }

    @Dado("que existe um agendamento confirmado para {string}")
    public void agendamentoConfirmado(String nome) {
        Agendamento ag = Agendamento.criar(
                agendamentoId,
                pacienteId,
                LocalDateTime.now().plusHours(1),
                true,
                false,
                true,
                "Recepcionista"
        );
        ag.confirmar("Recepcionista");

        agendamentoRepository.salvar(ag);
    }

    // =========================================================
    // CENÁRIO 1
    // =========================================================

    @Dado("que o paciente {string} não possui prontuário")
    public void pacienteSemProntuario(String nome) {
        // não salva nada → simula ausência
    }

    @Quando("o dentista registra a primeira ficha clínica para {string}")
    public void registrarPrimeiraFicha(String nome) {
        try {
            service.registrarAtendimento(pacienteId, agendamentoId, fichaId, "Primeira evolução");
        } catch (Exception e) {
            context.excecao = e;
        }
    }

    @Então("o sistema deve criar automaticamente um prontuário para {string}")
    public void validarCriacaoProntuario(String nome) {
        Optional<Prontuario> prontuario =
                prontuarioRepository.buscarPorPacienteId(pacienteId);

        assertTrue(prontuario.isPresent());
    }

    @Então("a ficha clínica deve ser vinculada ao prontuário criado")
    public void validarFichaNoProntuario() {
        Prontuario prontuario =
                prontuarioRepository.buscarPorPacienteId(pacienteId).get();

        assertEquals(1, prontuario.getFichas().size());
    }

    @Então("o prontuário deve ter status {string}")
    public void validarStatusProntuario(String status) {
        Prontuario prontuario =
                prontuarioRepository.buscarPorPacienteId(pacienteId).get();

        assertEquals(StatusProntuario.valueOf(status), prontuario.getStatus());
    }

    // =========================================================
    // CENÁRIO 3
    // =========================================================

    @Dado("que existe uma ficha clínica confirmada para {string} com a evolução {string}")
    public void fichaConfirmada(String nome, String evolucao) {
        Prontuario p = Prontuario.criar(prontuarioId, pacienteId);
        p.adicionarFicha(fichaId, evolucao);
        p.confirmarFicha(fichaId);

        prontuarioRepository.salvar(p);
    }

    @Quando("o dentista tenta editar a evolução {string}")
    public void tentarEditar(String evolucao) {
        try {
            service.editarEvolucao(prontuarioId, fichaId, "Nova evolução");
        } catch (Exception e) {
            context.excecao = e;
        }
    }

    // =========================================================
    // CENÁRIO 4
    // =========================================================

    @Dado("que não existe agendamento confirmado para {string} na data atual")
    public void semAgendamento(String nome) {
        // não cria agendamento
    }

    @Quando("o dentista tenta registrar uma ficha clínica para {string}")
    public void registrarSemAgendamento(String nome) {
        try {
            service.registrarAtendimento(pacienteId, agendamentoId, fichaId, "Teste");
        } catch (Exception e) {
            context.excecao = e;
        }
    }

    // =========================================================
    // CENÁRIO 5
    // =========================================================

    @Dado("que o prontuário de {string} tem status {string}")
    public void prontuarioAtivo(String nome, String status) {
        Prontuario p = Prontuario.criar(prontuarioId, pacienteId);
        prontuarioRepository.salvar(p);
    }

    @Quando("o dentista encerra o prontuário com a justificativa {string}")
    public void encerrarProntuario(String justificativa) {
        try {
            service.encerrarProntuario(pacienteId, justificativa);
        } catch (Exception e) {
            context.excecao = e;
        }
    }

    @Então("a justificativa deve ser registrada")
    public void validarJustificativa() {
        Prontuario p = prontuarioRepository.buscarPorPacienteId(pacienteId).get();
        assertNotNull(p.getJustificativaEncerramento());
    }

    // =========================================================
    // CENÁRIO 6
    // =========================================================

    @Quando("qualquer usuário tenta excluir o prontuário de {string}")
    public void excluirProntuario(String nome) {
        try {
            service.excluirProntuario(pacienteId);
        } catch (Exception e) {
            context.excecao = e;
        }
    }
}