package com.g4.odontohub.steps;

import io.cucumber.java.pt.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.g4.odontohub.prescricao.application.PrescricaoApplicationService;
import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;

import static org.junit.jupiter.api.Assertions.*;

public class F08PrescricaoSteps {

    private PrescricaoApplicationService prescricaoService;
    private Prescricao prescricaoAtual;
    private List<Prescricao> historicoPrescricoes;
    private Long pacienteIdAtual;
    private Long dentistaIdAtual;
    private List<ItemPrescricao> medicamentosAtual;
    private String observacoesAtual;

    @Dado("que o paciente {string} está cadastrado no sistema para prescrição")
    public void queOPacienteEstaCadastradoNoSistema(String nomePaciente) {
        prescricaoService = SharedTestServices.getPrescricaoService();
        pacienteIdAtual = 1L;
    }

    @E("que o dentista {string} está cadastrado no sistema para prescrição")
    public void queODentistaEstaCadastradoNoSistema(String nomeDentista) {
        dentistaIdAtual = 1L;
    }

    

    @Quando("o dentista {string} registra a prescrição para {string} com os medicamentos:")
    public void oDentistaRegistraAPrescricaoParaComOsMedicamentos(String nomeDentista, String nomePaciente,
            List<Map<String, String>> medicamentos) {
        prescricaoService = SharedTestServices.getPrescricaoService();

        medicamentosAtual = new ArrayList<>();
        for (Map<String, String> med : medicamentos) {
            String nome = med.get("Medicamento");
            String dosagem = med.get("Dosagem");
            String periodo = med.get("Período");
            medicamentosAtual.add(ItemPrescricao.criar(nome, dosagem, periodo));
        }
    }

    @E("adiciona a observação {string}")
    public void adicionaAObservacao(String observacao) {
        observacoesAtual = observacao;
        prescricaoAtual = prescricaoService.registrarPrescricao(
                pacienteIdAtual,
                dentistaIdAtual,
                medicamentosAtual,
                observacoesAtual);
    }

    @Então("a prescrição deve ser salva com a data de hoje")
    public void aPrescricaoDeveSerSalvaComADataDeHoje() {
        assertNotNull(prescricaoAtual, "Prescrição não foi salva");
        assertEquals(LocalDate.now(), prescricaoAtual.getDataPrescricao(),
                "Data da prescrição deve ser hoje");
    }

    @E("deve estar vinculada ao perfil de {string}")
    public void deveEstarVinculadaAoPerfilDe(String nomePaciente) {
        assertEquals(pacienteIdAtual, prescricaoAtual.getPacienteId().getId(),
                "Prescrição deve estar vinculada ao paciente");
    }

    @E("deve conter a observação terapêutica informada")
    public void deveConterAObservacaoTerapeuticaInformada() {
        assertEquals(observacoesAtual, prescricaoAtual.getObservacoesTerapeuticas(),
                "Observação terapêutica deve ser a mesma informada");
    }

    @Dado("que {string} possui {int} prescrições registradas em datas diferentes")
    public void quePossuiPrescricoesRegistradasEmDatasDiferentes(String nomePaciente, int quantidade) {
        prescricaoService = SharedTestServices.getPrescricaoService();
        pacienteIdAtual = 1L;

        // Criar prescrições em datas diferentes
        for (int i = 0; i < quantidade; i++) {
            List<ItemPrescricao> meds = new ArrayList<>();
            meds.add(ItemPrescricao.criar("Medicamento " + i, "1 comp", "5 dias"));
            prescricaoService.registrarPrescricao(pacienteIdAtual, 1L, meds, "Observação " + i);
        }
    }

    @Quando("o dentista consulta o histórico de prescrições de {string}")
    public void oDentistaConsultaOHistoricoDePrescricoesDe(String nomePaciente) {
        historicoPrescricoes = prescricaoService.listarPorPaciente(pacienteIdAtual);
    }

    @Então("as prescrições devem ser listadas em ordem cronológica decrescente")
    public void asPrescricoesDevemSerListadasEmOrdemCronologicaDecrescente() {
        assertNotNull(historicoPrescricoes, "Histórico não pode ser nulo");
        assertFalse(historicoPrescricoes.isEmpty(), "Histórico não pode estar vazio");

        LocalDate anterior = null;
        for (Prescricao p : historicoPrescricoes) {
            if (anterior != null) {
                assertTrue(p.getDataPrescricao().compareTo(anterior) <= 0,
                        "Prescrições devem estar em ordem cronológica decrescente");
            }
            anterior = p.getDataPrescricao();
        }
    }

    @Dado("que existe uma prescrição anterior de {string} registrada para {string}")
    public void queExisteUmaPrescricaoAnteriorDeRegistradaPara(String medicamento, String nomePaciente) {
        prescricaoService = SharedTestServices.getPrescricaoService();
        pacienteIdAtual = 1L;
        dentistaIdAtual = 1L;

        List<ItemPrescricao> meds = new ArrayList<>();
        meds.add(ItemPrescricao.criar(medicamento, "1 comp", "7 dias"));
        prescricaoAtual = prescricaoService.registrarPrescricao(pacienteIdAtual, dentistaIdAtual, meds, "Observação");
    }

    @Quando("o dentista repete a prescrição anterior para {string}")
    public void oDentistaRepeteAPrescricaoAnteriorPara(String nomePaciente) {
        Long prescricaoOrigemId = prescricaoAtual.getId().getId();
        Long novoDentistaId = 2L; // Novo dentista

        Prescricao novaPrescricao = prescricaoService.repetirPrescricao(prescricaoOrigemId, novoDentistaId);
        prescricaoAtual = novaPrescricao;
    }

    @Então("uma nova prescrição deve ser criada com os mesmos medicamentos")
    public void umaNovaPrescricaoDeveSerCriadaComOsMesmosMedicamentos() {
        assertNotNull(prescricaoAtual, "Nova prescrição não foi criada");
        assertFalse(prescricaoAtual.getItens().isEmpty(), "Nova prescrição deve ter medicamentos");
    }

    @E("a data da nova prescrição deve ser a data atual")
    public void aDataDaNovaPrescricaoDeveSerADataAtual() {
        assertEquals(LocalDate.now(), prescricaoAtual.getDataPrescricao(),
                "Data da nova prescrição deve ser a data atual");
    }

    @E("a nova prescrição deve registrar a referência à prescrição de origem")
    public void aNovaPrescricaoDeveRegistrarAReferenciaAPrescricaoDeOrigem() {
        assertNotNull(prescricaoAtual.getPrescricaoOrigemId(),
                "Nova prescrição deve ter referência à prescrição de origem");
    }

    @Dado("que o dentista {string} prescreveu {string} em janeiro de {int}")
    public void queODentistaPrescreveuEmJaneiroDe(String nomeDentista, String medicamento, int ano) {
        // Não reinicializar o serviço para manter as prescrições entre cenários
        if (prescricaoService == null) {
            prescricaoService = SharedTestServices.getPrescricaoService();
        }
        Long dentistId = 1L;

        List<ItemPrescricao> meds = new ArrayList<>();
        meds.add(ItemPrescricao.criar(medicamento, "1 comp", "7 dias"));

        // Criar prescrição com data de janeiro de 2026
        LocalDate dataJaneiro = LocalDate.of(ano, 1, 15);
        Prescricao prescricao = prescricaoService.registrarPrescricaoComData(1L, dentistId, meds, "Observação",
                dataJaneiro);
    }

    @E("que o dentista {string} prescreveu {string} em fevereiro de {int}")
    public void queODentistaPrescreveuEmFevereiroDe(String nomeDentista, String medicamento, int ano) {
        // Não reinicializar o serviço para manter as prescrições entre cenários
        if (prescricaoService == null) {
            prescricaoService = SharedTestServices.getPrescricaoService();
        }
        Long dentistId = 1L;

        List<ItemPrescricao> meds = new ArrayList<>();
        meds.add(ItemPrescricao.criar(medicamento, "1 comp", "5 dias"));

        // Criar prescrição com data de fevereiro de 2026
        LocalDate dataFevereiro = LocalDate.of(ano, 2, 10);
        Prescricao prescricao = prescricaoService.registrarPrescricaoComData(1L, dentistId, meds, "Observação",
                dataFevereiro);
    }

    @Quando("o dentista filtra suas prescrições do período de janeiro de {int}")
    public void oDentistaFiltraSuasPrescricoesDoPeriodoDeJaneiroDe(int ano) {
        Long dentistId = 1L;
        LocalDate dataInicio = LocalDate.of(ano, 1, 1);
        LocalDate dataFim = LocalDate.of(ano, 1, 31);

        historicoPrescricoes = prescricaoService.filtrarPorPeriodoEDentista(dentistId, dataInicio, dataFim);
    }

    @Então("apenas a prescrição de {string} deve ser retornada")
    public void apenasAPrescricaoDeDeveSerRetornada(String medicamento) {
        assertNotNull(historicoPrescricoes, "Resultado do filtro não pode ser nulo");
        assertFalse(historicoPrescricoes.isEmpty(), "Deve retornar pelo menos uma prescrição");

        boolean encontrou = false;
        for (Prescricao p : historicoPrescricoes) {
            for (ItemPrescricao item : p.getItens()) {
                if (item.getNomeMedicamento().equals(medicamento)) {
                    encontrou = true;
                    break;
                }
            }
        }
        assertTrue(encontrou, "Prescrição de " + medicamento + " deve estar nos resultados");
    }

    @E("a prescrição de {string} não deve aparecer nos resultados")
    public void aPrescricaoDeNaoDeveAparecerNosResultados(String medicamento) {
        for (Prescricao p : historicoPrescricoes) {
            for (ItemPrescricao item : p.getItens()) {
                assertNotEquals(medicamento, item.getNomeMedicamento(),
                        "Prescrição de " + medicamento + " não deve aparecer nos resultados");
            }
        }
    }
}