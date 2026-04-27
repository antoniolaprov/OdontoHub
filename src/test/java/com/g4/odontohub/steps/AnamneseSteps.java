package com.g4.odontohub.steps;

import com.g4.odontohub.infra.InMemoryAnamneseRepository;
import com.g4.odontohub.prontuario.application.AnamneseService;
import com.g4.odontohub.prontuario.domain.Anamnese;
import com.g4.odontohub.shared.exception.DomainException;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AnamneseSteps {

    private final InMemoryAnamneseRepository repo = new InMemoryAnamneseRepository();
    private final AnamneseService service = new AnamneseService(repo);

    private Anamnese anamneseResultado;
    private static final Long PACIENTE_ID = 1L;

    // ── Pré-condições ────────────────────────────────────────────────────────

    @Dado("que o paciente {string} não possui anamnese registrada")
    public void pacienteSemAnamnese(String paciente) {
        // repo já está vazio — no-op
    }

    @Dado("que a anamnese de {string} registra alergia a {string}")
    public void anamneseComAlergia(String paciente, String alergia) {
        anamneseResultado = service.registrar(PACIENTE_ID, List.of(alergia), List.of(), "dentista");
    }

    @Dado("que o paciente {string} possui anamnese na versão {int}")
    public void pacienteComAnamneseNaVersao(String paciente, int versao) {
        anamneseResultado = service.registrar(PACIENTE_ID, List.of(), List.of(), "dentista");
        assertEquals(versao, anamneseResultado.getVersao());
    }

    @Dado("que o paciente {string} possui anamnese registrada")
    public void pacienteComAnamneseRegistrada(String paciente) {
        anamneseResultado = service.registrar(PACIENTE_ID, List.of(), List.of(), "dentista");
    }

    // ── Ações ────────────────────────────────────────────────────────────────

    @Quando("o dentista registra a anamnese com alergia a {string} e contraindicação a {string}")
    public void registrarAnamnese(String alergia, String contraindicacao) {
        try {
            anamneseResultado = service.registrar(
                    PACIENTE_ID, List.of(alergia), List.of(contraindicacao), "dentista");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista tenta criar um plano de tratamento para {string}")
    public void tentarCriarPlanoSemAnamnese(String paciente) {
        try {
            service.validarAnamneseParaPlano(PACIENTE_ID);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista tenta registrar um procedimento que utiliza {string} para {string}")
    public void tentarProcedimentoComSubstancia(String substancia, String paciente) {
        try {
            service.verificarAlergiaParaProcedimento(PACIENTE_ID, substancia);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista atualiza a anamnese adicionando alergia a {string}")
    public void atualizarAdicionandoAlergia(String novaAlergia) {
        try {
            anamneseResultado = service.adicionarAlergia(PACIENTE_ID, novaAlergia, "dentista");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("a dentista {string} atualiza as condições sistêmicas da anamnese")
    public void atualizarCondicoesSistemicas(String responsavel) {
        try {
            anamneseResultado = service.atualizarCondicoesSistemicas(
                    PACIENTE_ID, "Hipertensão controlada", responsavel);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    // ── Verificações ─────────────────────────────────────────────────────────

    @Então("a anamnese deve ser salva com as informações fornecidas")
    public void anamneseDeveSalva() {
        assertNull(ScenarioContext.get().excecao);
        assertNotNull(anamneseResultado);
        assertFalse(anamneseResultado.getAlergias().isEmpty());
        assertFalse(anamneseResultado.getContraindicacoes().isEmpty());
    }

    @Então("a data e o responsável pelo registro devem ser gravados")
    public void dataEResponsavelGravados() {
        assertNotNull(anamneseResultado.getDataRegistro());
        assertNotNull(anamneseResultado.getResponsavel());
    }

    @Então("o sistema deve bloquear a criação do plano")
    public void sistemaBloqueiaPlano() {
        assertNotNull(ScenarioContext.get().excecao);
    }

    @Então("o sistema deve exibir um alerta de alergia")
    public void sistemaExibeAlertaAlergia() {
        assertNotNull(ScenarioContext.get().excecao);
    }

    @Então("a anamnese deve ser atualizada para a versão {int}")
    public void anamneseDeveEstarNaVersao(int versaoEsperada) {
        assertEquals(versaoEsperada, anamneseResultado.getVersao());
    }

    @Então("o histórico deve preservar a versão {int} com data e responsável pela alteração")
    public void historicoDevePreservarVersao(int versao) {
        assertTrue(anamneseResultado.getHistorico().stream()
                .anyMatch(h -> h.getVersao() == versao
                        && h.getDataAlteracao() != null
                        && h.getResponsavel() != null));
    }

    @Então("a atualização deve registrar a data atual e o nome {string} como responsável")
    public void atualizacaoDeveRegistrarResponsavel(String responsavel) {
        assertEquals(responsavel, anamneseResultado.getResponsavel());
        assertNotNull(anamneseResultado.getDataUltimaAtualizacao());
        assertEquals(LocalDate.now(), anamneseResultado.getDataUltimaAtualizacao().toLocalDate());
    }
}
