package com.g4.odontohub.steps;

import com.g4.odontohub.prescricao.application.PrescricaoApplicationService;
import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * F08 — Registro de Medicamentos Prescritos (prescrição).
 *
 * <p>Os steps de "paciente/dentista cadastrado no sistema" (Contexto) já são
 * definidos globalmente por {@code F01AgendamentoSteps}; aqui só tratamos a
 * prescrição em si, ligando ao {@link PrescricaoApplicationService} real.</p>
 */
public class F08PrescricaoSteps {

    private final PrescricaoApplicationService service = new PrescricaoApplicationService();

    private Prescricao ultima;
    private Prescricao nova;
    private Long origemId;
    private String observacaoEsperada;
    private String medicamentoOrigem;
    private List<Prescricao> resultado = new ArrayList<>();

    @Quando("o dentista {string} registra a prescrição para {string} com os medicamentos:")
    public void registraPrescricao(String dentista, String paciente, DataTable tabela) {
        List<ItemPrescricao> itens = new ArrayList<>();
        for (Map<String, String> linha : tabela.asMaps(String.class, String.class)) {
            itens.add(new ItemPrescricao(
                    linha.get("Medicamento"), linha.get("Dosagem"), linha.get("Período")));
        }
        ultima = service.registrarPrescricao(paciente, dentista, itens, null);
    }

    @E("adiciona a observação {string}")
    public void adicionaObservacao(String observacao) {
        observacaoEsperada = observacao;
        ultima.definirObservacoes(observacao);
    }

    @Então("a prescrição deve ser salva com a data de hoje")
    public void prescricaoSalvaComDataDeHoje() {
        assertNotNull(ultima);
        assertEquals(LocalDate.now(), ultima.getDataPrescricao());
    }

    @E("deve estar vinculada ao perfil de {string}")
    public void vinculadaAoPerfil(String paciente) {
        assertEquals(paciente, ultima.getPaciente());
    }

    @E("deve conter a observação terapêutica informada")
    public void contemObservacao() {
        assertEquals(observacaoEsperada, ultima.getObservacoesTerapeuticas());
    }

    @Dado("que {string} possui {int} prescrições registradas em datas diferentes")
    public void possuiPrescricoesEmDatasDiferentes(String paciente, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            service.registrarPrescricaoComData(paciente, "Dr. Carlos",
                    List.of(new ItemPrescricao("Medicamento " + i, "1 comp", "5 dias")),
                    null, LocalDate.now().minusDays(i));
        }
    }

    @Quando("o dentista consulta o histórico de prescrições de {string}")
    public void consultaHistorico(String paciente) {
        resultado = service.listarPorPaciente(paciente);
    }

    @Então("as prescrições devem ser listadas em ordem cronológica decrescente")
    public void listadasEmOrdemDecrescente() {
        assertFalse(resultado.isEmpty());
        for (int i = 0; i < resultado.size() - 1; i++) {
            assertFalse(resultado.get(i).getDataPrescricao()
                            .isBefore(resultado.get(i + 1).getDataPrescricao()),
                    "Lista deveria estar em ordem cronológica decrescente");
        }
    }

    @Dado("que existe uma prescrição anterior de {string} registrada para {string}")
    public void existePrescricaoAnterior(String medicamento, String paciente) {
        medicamentoOrigem = medicamento;
        ultima = service.registrarPrescricao(paciente, "Dr. Carlos",
                List.of(new ItemPrescricao(medicamento, "1 comp", "7 dias")),
                "Observação original");
        origemId = ultima.getId().id();
    }

    @Quando("o dentista repete a prescrição anterior para {string}")
    public void repetePrescricao(String paciente) {
        nova = service.repetirPrescricao(origemId, "Dr. Carlos");
    }

    @Então("uma nova prescrição deve ser criada com os mesmos medicamentos")
    public void novaComMesmosMedicamentos() {
        assertNotNull(nova);
        assertTrue(nova.contemMedicamento(medicamentoOrigem));
        assertEquals(ultima.getItens().size(), nova.getItens().size());
    }

    @E("a data da nova prescrição deve ser a data atual")
    public void novaComDataAtual() {
        assertEquals(LocalDate.now(), nova.getDataPrescricao());
    }

    @E("a nova prescrição deve registrar a referência à prescrição de origem")
    public void novaReferenciaOrigem() {
        assertEquals(origemId, nova.getPrescricaoOrigemId());
    }

    @Dado("que o dentista {string} prescreveu {string} em janeiro de {int}")
    public void prescreveuEmJaneiro(String dentista, String medicamento, int ano) {
        service.registrarPrescricaoComData("Roberto Lima", dentista,
                List.of(new ItemPrescricao(medicamento, "1 comp", "5 dias")),
                null, LocalDate.of(ano, 1, 15));
    }

    @E("que o dentista {string} prescreveu {string} em fevereiro de {int}")
    public void prescreveuEmFevereiro(String dentista, String medicamento, int ano) {
        service.registrarPrescricaoComData("Roberto Lima", dentista,
                List.of(new ItemPrescricao(medicamento, "1 comp", "5 dias")),
                null, LocalDate.of(ano, 2, 15));
    }

    @Quando("o dentista filtra suas prescrições do período de janeiro de {int}")
    public void filtraPorJaneiro(int ano) {
        resultado = service.filtrarPorPeriodoEDentista("Dr. Carlos", 1, ano);
    }

    @Então("apenas a prescrição de {string} deve ser retornada")
    public void apenasPrescricaoDe(String medicamento) {
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).contemMedicamento(medicamento));
    }

    @E("a prescrição de {string} não deve aparecer nos resultados")
    public void prescricaoNaoApareceNosResultados(String medicamento) {
        assertTrue(resultado.stream().noneMatch(p -> p.contemMedicamento(medicamento)));
    }
}
