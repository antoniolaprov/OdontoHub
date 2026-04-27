package com.g4.odontohub.steps;

import com.g4.odontohub.manutencao.application.EquipamentoService;
import com.g4.odontohub.manutencao.domain.*;
import com.g4.odontohub.infra.InMemoryEquipamentoRepository;
import com.g4.odontohub.infra.InMemoryRegistroManutencaoRepository;
import com.g4.odontohub.shared.exception.DomainException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class F14ManutencaoSteps {

    private EquipamentoService service;
    private InMemoryEquipamentoRepository equipamentoRepository;
    private InMemoryRegistroManutencaoRepository manutencaoRepository;

    private List<AlertaManutencao> alertas;

    @Before
    public void setup() {
        equipamentoRepository = new InMemoryEquipamentoRepository();
        manutencaoRepository = new InMemoryRegistroManutencaoRepository();
        service = new EquipamentoService(equipamentoRepository, manutencaoRepository);
    }

    @Dado("que o equipamento {string} está cadastrado com status {string}")
    public void equipamentoCadastradoComStatus(String nome, String status) {
        Equipamento e = Equipamento.criar(nome, StatusEquipamento.valueOf(status), null);
        equipamentoRepository.salvar(e);
    }

    @Dado("com periodicidade de manutenção de {int} dias")
    public void comPeriodicidade(int dias) {
        Equipamento e = equipamentoRepository.buscarPorNome("Cadeira Odontológica");
        e.definirPeriodicidade(dias);
        equipamentoRepository.salvar(e);
    }

    @Dado("que a {string} tem status {string}")
    public void equipamentoTemStatus(String nome, String status) {
        Equipamento e = equipamentoRepository.buscarPorNome(nome);
        StatusEquipamento targetStatus = StatusEquipamento.valueOf(status);
        if (e == null) {
            e = Equipamento.criar(nome, targetStatus, null);
        } else if (targetStatus == StatusEquipamento.EM_MANUTENCAO) {
            e.iniciarManutencao();
        } else if (targetStatus == StatusEquipamento.DISPONIVEL) {
            e.concluirManutencao(LocalDate.now());
        }
        equipamentoRepository.salvar(e);
    }

    @Dado("que a próxima manutenção da {string} está agendada para {string}")
    public void proximaManutencaoAgendada(String nome, String data) {
        Equipamento e = equipamentoRepository.buscarPorNome(nome);
        LocalDate proxima = LocalDate.parse(data);
        if (e.getPeriodicidadeDias() != null) {
            e.concluirManutencao(proxima.minusDays(e.getPeriodicidadeDias()));
        }
        equipamentoRepository.salvar(e);
    }

    @Dado("que o equipamento {string} está cadastrado sem periodicidade definida")
    public void equipamentoCadastradoSemPeriodicidade(String nome) {
        Equipamento e = Equipamento.criar(nome, StatusEquipamento.DISPONIVEL, null);
        equipamentoRepository.salvar(e);
    }

    @Quando("o dentista registra o início de uma manutenção corretiva na {string}")
    public void iniciarManutencaoCorretiva(String nome) {
        try {
            service.iniciarManutencaoCorretiva(nome);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista tenta vincular o equipamento {string} a um procedimento")
    public void tentaVincularEquipamento(String nome) {
        try {
            service.vincularEquipamentoAProcedimento(nome);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista registra a conclusão da manutenção em {string}")
    public void concluirManutencao(String data) {
        try {
            service.concluirManutencao("Cadeira Odontológica", LocalDate.parse(data));
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o sistema verifica os equipamentos na data {string}")
    public void sistemaVerificaEquipamentos(String data) {
        alertas = service.verificarAlertasManutencao("Cadeira Odontológica", LocalDate.parse(data));
    }

    @Quando("o dentista registra uma manutenção preventiva na {string} com:")
    public void registrarManutencaoComDados(String nome, DataTable tabela) {
        try {
            Map<String, String> dados = tabela.asMap(String.class, String.class);
            TipoManutencao tipo = TipoManutencao.valueOf(dados.get("Tipo"));
            LocalDate dataInicio = LocalDate.parse(dados.get("Data de Início"));
            String responsavel = dados.get("Responsável Técnico");
            String descricao = dados.get("Descrição");
            String custoStr = dados.get("Custo").replace("R$ ", "").replace(",", ".");
            BigDecimal custo = new BigDecimal(custoStr);
            service.registrarManutencao(nome, tipo, dataInicio, responsavel, descricao, custo);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista tenta registrar uma manutenção sem informar o responsável técnico")
    public void tentaRegistrarSemResponsavel() {
        try {
            Equipamento e = equipamentoRepository.buscarPorNome("Cadeira Odontológica");
            service.registrarManutencao(e.getNome(), TipoManutencao.PREVENTIVA,
                    LocalDate.now(), null, "Descrição qualquer", BigDecimal.ZERO);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Quando("o dentista define a periodicidade de manutenção do {string} como {int} dias")
    public void definirPeriodicidade(String nome, int dias) {
        try {
            service.definirPeriodicidade(nome, dias);
        } catch (DomainException e) {
            ScenarioContext.get().excecao = e;
        }
    }

    @Então("o equipamento {string} deve ter status {string}")
    public void equipamentoDeveTermStatus(String nome, String statusEsperado) {
        Equipamento e = equipamentoRepository.buscarPorNome(nome);
        assertNotNull(e, "Equipamento não encontrado: " + nome);
        assertEquals(StatusEquipamento.valueOf(statusEsperado), e.getStatus());
    }

    @Então("o equipamento não deve poder ser associado a novos procedimentos")
    public void equipamentoNaoDevePoderSerAssociado() {
        Equipamento e = equipamentoRepository.buscarPorNome("Cadeira Odontológica");
        assertThrows(DomainException.class, e::verificarDisponibilidadeParaProcedimento);
    }

    @Então("o sistema deve bloquear a seleção do equipamento")
    public void bloquearSelecaoEquipamento() {
        assertNotNull(ScenarioContext.get().excecao, "Deveria ter bloqueado a seleção do equipamento");
    }

    @Então("a data da próxima manutenção deve ser recalculada para {string}")
    public void proximaManutencaoRecalculada(String dataEsperada) {
        Equipamento e = equipamentoRepository.buscarPorNome("Cadeira Odontológica");
        assertEquals(LocalDate.parse(dataEsperada), e.getProximaManutencao());
    }

    @Então("o sistema deve emitir um alerta ao dentista informando que a manutenção está próxima")
    public void deveEmitirAlerta() {
        assertNotNull(alertas);
        assertFalse(alertas.isEmpty(), "Deveria ter emitido pelo menos um alerta");
    }

    @Então("o alerta deve indicar {int} dias restantes")
    public void alertaDeveIndicarDias(int diasEsperados) {
        assertFalse(alertas.isEmpty(), "Nenhum alerta foi emitido");
        assertEquals((long) diasEsperados, alertas.get(0).getDiasRestantes());
    }

    @Então("nenhum alerta deve ser emitido para a {string}")
    public void nenhumAlertaEmitido(String nome) {
        assertNotNull(alertas);
        assertTrue(alertas.isEmpty(), "Não deveria ter emitido alertas para " + nome);
    }

    @Então("a manutenção deve ser salva e vinculada ao histórico patrimonial da {string}")
    public void manutencaoSalvaNoHistorico(String nome) {
        Equipamento e = equipamentoRepository.buscarPorNome(nome);
        assertNotNull(e);
        List<RegistroManutencao> registros = manutencaoRepository.buscarPorEquipamento(e.getId());
        assertFalse(registros.isEmpty(), "Deveria ter ao menos um registro de manutenção vinculado");
    }

    @Então("a periodicidade do {string} deve ser salva como {int} dias")
    public void periodicidadeSalva(String nome, int diasEsperados) {
        Equipamento e = equipamentoRepository.buscarPorNome(nome);
        assertNotNull(e);
        assertEquals(diasEsperados, e.getPeriodicidadeDias());
    }

    @Então("a data da próxima manutenção deve ser calculada a partir da última manutenção registrada")
    public void proximaManutencaoCalculadaDaUltima() {
        Equipamento e = equipamentoRepository.buscarPorNome("Compressor");
        assertNotNull(e);
        if (e.getUltimaManutencao() != null) {
            assertNotNull(e.getProximaManutencao());
        }
    }
}