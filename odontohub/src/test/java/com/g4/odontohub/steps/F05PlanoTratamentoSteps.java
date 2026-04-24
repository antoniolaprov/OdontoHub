package com.g4.odontohub.steps;

import com.g4.odontohub.infra.InMemoryPlanoTratamentoRepository;
import com.g4.odontohub.shared.exception.DomainException;
import com.g4.odontohub.tratamento.application.PlanoTratamentoService;
import com.g4.odontohub.tratamento.application.ProntuarioACL;
import com.g4.odontohub.tratamento.domain.PlanoTratamento;
import com.g4.odontohub.tratamento.domain.Procedimento;
import com.g4.odontohub.tratamento.domain.StatusPlano;
import com.g4.odontohub.tratamento.domain.StatusProcedimento;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class F05PlanoTratamentoSteps {

    private PlanoTratamentoService service;
    private InMemoryPlanoTratamentoRepository repository;
    private boolean pacienteTemAnamneseMock;
    
    private final Long PACIENTE_MOCK_ID = 1L;
    private final Long PLANO_MOCK_ID = 100L;

    @Before
    public void setup() {
        repository = new InMemoryPlanoTratamentoRepository();
        ProntuarioACL mockACL = pacienteId -> pacienteTemAnamneseMock;
        service = new PlanoTratamentoService(repository, mockACL);
    }

    @Quando("o dentista cria um plano de tratamento com os procedimentos {string} e {string}")
    public void cria_plano_tratamento(String proc1, String proc2) {
        this.pacienteTemAnamneseMock = true; // FIX: Forçando o mock da anamnese para passar na validação
        List<Procedimento> procedimentos = List.of(
            Procedimento.criar(1L, proc1),
            Procedimento.criar(2L, proc2)
        );
        try {
            service.criarPlano(PLANO_MOCK_ID, PACIENTE_MOCK_ID, procedimentos);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }
    @Quando("o dentista tenta criar um plano de tratamento para o paciente {string}")
    public void tenta_criar_plano_paciente(String nome) {
        this.pacienteTemAnamneseMock = false; // Negando a anamnese
        try {
            service.criarPlano(PLANO_MOCK_ID, PACIENTE_MOCK_ID, new ArrayList<>());
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("o plano de tratamento deve ser criado com status {string}")
    public void plano_criado_status(String statusEsperado) {
        PlanoTratamento plano = repository.buscarPorId(PLANO_MOCK_ID);
        assertEquals(StatusPlano.valueOf(statusEsperado), plano.getStatus());
    }

    @Então("os procedimentos devem ter status inicial {string}")
    public void procedimentos_status_inicial(String statusEsperado) {
        PlanoTratamento plano = repository.buscarPorId(PLANO_MOCK_ID);
        plano.getProcedimentos().forEach(p -> 
            assertEquals(StatusProcedimento.valueOf(statusEsperado), p.getStatus())
        );
    }

    @Então("o plano deve ser registrado na versão {int}")
    public void plano_versao(Integer versaoEsperada) {
        PlanoTratamento plano = repository.buscarPorId(PLANO_MOCK_ID);
        assertEquals(versaoEsperada, plano.getVersaoAtual());
    }

    @Dado("que existe um plano de tratamento ativo com o procedimento {string} com status {string}")
    public void existe_plano_ativo_com_procedimento(String descProc, String statusProc) {
        this.pacienteTemAnamneseMock = true;
        Procedimento p = Procedimento.criar(1L, descProc);
        if (statusProc.equals("REALIZADO")) p.realizar();
        
        service.criarPlano(PLANO_MOCK_ID, PACIENTE_MOCK_ID, List.of(p));
    }

    @Quando("o dentista cancela o procedimento {string} sem informar justificativa")
    public void cancela_procedimento_sem_justificativa(String descProc) {
        try {
            service.cancelarProcedimento(PLANO_MOCK_ID, descProc, "");
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista cancela o procedimento {string} com a justificativa {string}")
    public void cancela_procedimento_com_justificativa(String descProc, String justificativa) {
        try {
            service.cancelarProcedimento(PLANO_MOCK_ID, descProc, justificativa);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("o status do procedimento deve ser alterado para {string}")
    public void status_procedimento_alterado(String statusEsperado) {
        PlanoTratamento plano = repository.buscarPorId(PLANO_MOCK_ID);
        Procedimento p = plano.getProcedimentos().get(0);
        assertEquals(StatusProcedimento.valueOf(statusEsperado), p.getStatus());
    }

    @Então("a justificativa {string} deve ser registrada")
    public void justificativa_registrada(String justificativaEsperada) {
        PlanoTratamento plano = repository.buscarPorId(PLANO_MOCK_ID);
        Procedimento p = plano.getProcedimentos().get(0);
        assertEquals(justificativaEsperada, p.getJustificativaCancelamento());
    }

    @Dado("que existe um plano de tratamento com os procedimentos:")
    public void plano_com_procedimentos(DataTable dataTable) {
        this.pacienteTemAnamneseMock = true;
        List<Procedimento> procedimentos = new ArrayList<>();
        long idCounter = 1L;
        
        List<Map<String, String>> linhas = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> linha : linhas) {
            Procedimento p = Procedimento.criar(idCounter++, linha.get("Procedimento"));
            if (linha.get("Status").equals("REALIZADO")) {
                p.realizar();
            }
            procedimentos.add(p);
        }
        service.criarPlano(PLANO_MOCK_ID, PACIENTE_MOCK_ID, procedimentos);
    }

    @Quando("o dentista marca o plano como concluído")
    @Quando("o dentista tenta marcar o plano como concluído")
    public void tenta_concluir_plano() {
        try {
            service.concluirPlano(PLANO_MOCK_ID);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Dado("que existe um plano de tratamento na versão {int} para {string}")
    public void existe_plano_na_versao(Integer versao, String nome) {
        this.pacienteTemAnamneseMock = true;
        service.criarPlano(PLANO_MOCK_ID, PACIENTE_MOCK_ID, new ArrayList<>());
    }

    @Quando("o dentista adiciona o procedimento {string} ao plano em um retorno")
    public void adiciona_procedimento_retorno(String descProc) {
        try {
            service.adicionarProcedimentoRetorno(PLANO_MOCK_ID, Procedimento.criar(99L, descProc));
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("o histórico deve preservar os dados da versão {int}")
    public void historico_preservado(Integer versaoAntiga) {
        PlanoTratamento plano = repository.buscarPorId(PLANO_MOCK_ID);
        assertEquals("Versão " + versaoAntiga + " salva com sucesso", plano.getHistoricoVersoes().get(0));
    }

    @Quando("qualquer usuário tenta excluir o plano de tratamento")
    public void tenta_excluir_plano() {
        try {
            service.excluirPlano(PLANO_MOCK_ID);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("o sistema deve bloquear a criação")
    public void sistema_bloqueia_criacao() {
        org.junit.jupiter.api.Assertions.assertNotNull(ScenarioContext.get().excecao, "Deveria ter lançado uma exceção de bloqueio de criação");
    }

    @Então("o sistema deve rejeitar o cancelamento")
    public void sistema_rejeita_cancelamento() {
        org.junit.jupiter.api.Assertions.assertNotNull(ScenarioContext.get().excecao, "Deveria ter lançado exceção de bloqueio de cancelamento");
    }

    @Então("o sistema deve rejeitar a conclusão")
    public void sistema_rejeita_conclusao() {
        org.junit.jupiter.api.Assertions.assertNotNull(ScenarioContext.get().excecao, "Deveria ter lançado exceção de bloqueio de conclusão");
    }

    @Então("o status do plano deve ser alterado para {string}")
    public void status_plano_alterado(String statusEsperado) {
        PlanoTratamento plano = repository.buscarPorId(PLANO_MOCK_ID);
        org.junit.jupiter.api.Assertions.assertEquals(StatusPlano.valueOf(statusEsperado), plano.getStatus());
    }

    @Então("o plano deve ser atualizado para a versão {int}")
    public void plano_atualizado_versao(Integer versaoEsperada) {
        PlanoTratamento plano = repository.buscarPorId(PLANO_MOCK_ID);
        org.junit.jupiter.api.Assertions.assertEquals(versaoEsperada, plano.getVersaoAtual());
    }

    @Dado("que existe um plano de tratamento com o procedimento {string} com status {string}")
    public void plano_com_procedimento_status_exato(String descProc, String statusProc) {
        this.pacienteTemAnamneseMock = true;
        Procedimento p = Procedimento.criar(1L, descProc);
        if (statusProc.equals("REALIZADO")) p.realizar();
        
        service.criarPlano(PLANO_MOCK_ID, PACIENTE_MOCK_ID, List.of(p));
    }
}