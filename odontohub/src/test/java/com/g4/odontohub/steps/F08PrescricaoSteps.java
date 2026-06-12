package com.g4.odontohub.steps;

import com.g4.odontohub.prescricao.application.PrescricaoApplicationService;
import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class F08PrescricaoSteps {

    private static final String PACIENTE_PADRAO = "Roberto Lima";

    private final PrescricaoApplicationService service = new PrescricaoApplicationService();
    private Prescricao ultimaPrescricao;
    private Prescricao prescricaoAnterior;
    private List<Prescricao> resultado;

    @Quando("o dentista {string} registra a prescrição para {string} com os medicamentos:")
    public void registrarPrescricaoComTabela(String dentista, String paciente, DataTable tabela) {
        List<ItemPrescricao> itens = tabela.asMaps().stream()
                .map(this::paraItem)
                .collect(Collectors.toList());
        ultimaPrescricao = service.registrarPrescricao(paciente, dentista, itens, "");
    }

    @E("adiciona a observação {string}")
    public void adicionaObservacao(String observacao) {
        ultimaPrescricao.definirObservacoes(observacao);
    }

    @Então("a prescrição deve ser salva com a data de hoje")
    public void prescricaoSalvaComDataDeHoje() {
        assertNotNull(ultimaPrescricao);
        assertEquals(LocalDate.now(), ultimaPrescricao.getDataPrescricao());
    }

    @E("deve estar vinculada ao perfil de {string}")
    public void deveEstarVinculadaAoPerfil(String paciente) {
        assertEquals(paciente, ultimaPrescricao.getPaciente());
    }

    @E("deve conter a observação terapêutica informada")
    public void deveConterObservacao() {
        assertNotNull(ultimaPrescricao.getObservacoesTerapeuticas());
        assertFalse(ultimaPrescricao.getObservacoesTerapeuticas().isBlank());
    }

    @Dado("que {string} possui 3 prescrições registradas em datas diferentes")
    public void possuiTresPrescricoes(String paciente) {
        for (int i = 3; i >= 1; i--) {
            service.registrarPrescricaoComData(paciente, "Dr. Carlos",
                    List.of(new ItemPrescricao("Medicamento " + i, "1 comp", "5 dias")),
                    "", LocalDate.now().minusDays(i));
        }
    }

    @Quando("o dentista consulta o histórico de prescrições de {string}")
    public void consultaHistorico(String paciente) {
        resultado = service.listarPorPaciente(paciente);
    }

    @Então("as prescrições devem ser listadas em ordem cronológica decrescente")
    public void listadasEmOrdemDecrescente() {
        assertNotNull(resultado);
        for (int i = 0; i < resultado.size() - 1; i++) {
            assertTrue(!resultado.get(i).getDataPrescricao().isBefore(resultado.get(i + 1).getDataPrescricao()),
                    "As prescrições não estão em ordem cronológica decrescente");
        }
    }

    @Dado("que existe uma prescrição anterior de {string} registrada para {string}")
    public void existePrescricaoAnterior(String medicamento, String paciente) {
        prescricaoAnterior = service.registrarPrescricaoComData(paciente, "Dr. Carlos",
                List.of(new ItemPrescricao(medicamento, "1 comp", "7 dias")), "", LocalDate.now().minusDays(10));
    }

    @Quando("o dentista repete a prescrição anterior para {string}")
    public void repeteAPrescricaoAnterior(String paciente) {
        ultimaPrescricao = service.repetirPrescricao(prescricaoAnterior.getId().id(), "Dr. Carlos");
    }

    @Então("uma nova prescrição deve ser criada com os mesmos medicamentos")
    public void novaPrescricaoComMesmosMedicamentos() {
        assertEquals(prescricaoAnterior.getItens(), ultimaPrescricao.getItens());
    }

    @E("a data da nova prescrição deve ser a data atual")
    public void dataDaNovaPrescricaoAtual() {
        assertEquals(LocalDate.now(), ultimaPrescricao.getDataPrescricao());
    }

    @E("a nova prescrição deve registrar a referência à prescrição de origem")
    public void registraReferenciaOrigem() {
        assertEquals(prescricaoAnterior.getId().id(), ultimaPrescricao.getPrescricaoOrigemId());
    }

    @Dado("que o dentista {string} prescreveu {string} em janeiro de {int}")
    public void prescreveuEmJaneiro(String dentista, String medicamento, int ano) {
        service.registrarPrescricaoComData(PACIENTE_PADRAO, dentista,
                List.of(new ItemPrescricao(medicamento, "1 comp", "7 dias")), "", LocalDate.of(ano, 1, 15));
    }

    @E("que o dentista {string} prescreveu {string} em fevereiro de {int}")
    public void prescreveuEmFevereiro(String dentista, String medicamento, int ano) {
        service.registrarPrescricaoComData(PACIENTE_PADRAO, dentista,
                List.of(new ItemPrescricao(medicamento, "1 comp", "7 dias")), "", LocalDate.of(ano, 2, 10));
    }

    @Quando("o dentista filtra suas prescrições do período de janeiro de {int}")
    public void filtraPrescricoesDeJaneiro(int ano) {
        resultado = service.filtrarPorPeriodoEDentista("Dr. Carlos", 1, ano);
    }

    @Então("apenas a prescrição de {string} deve ser retornada")
    public void apenasPrescricaoDeveSerRetornada(String medicamento) {
        assertNotNull(resultado);
        assertTrue(resultado.stream().anyMatch(p -> p.contemMedicamento(medicamento)),
                "A prescrição de " + medicamento + " deveria ser retornada");
    }

    @E("a prescrição de {string} não deve aparecer nos resultados")
    public void prescricaoNaoDeveAparecer(String medicamento) {
        assertTrue(resultado.stream().noneMatch(p -> p.contemMedicamento(medicamento)),
                "A prescrição de " + medicamento + " não deveria aparecer");
    }

    private ItemPrescricao paraItem(Map<String, String> linha) {
        return new ItemPrescricao(linha.get("Medicamento"), linha.get("Dosagem"), linha.get("Período"));
    }
}
