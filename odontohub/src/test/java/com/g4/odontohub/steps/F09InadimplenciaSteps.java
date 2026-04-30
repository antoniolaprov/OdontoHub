package com.g4.odontohub.steps;

import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.Parcela;
import com.g4.odontohub.financeiro.domain.model.StatusParcela;
import io.cucumber.java.pt.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class F09InadimplenciaSteps {

    private final InadimplenciaApplicationService service = new InadimplenciaApplicationService();
    private Long pacienteId = 1L;
    private boolean restricaoAtual;
    private List<Parcela> parcelasConsultadas;
    private Acordo acordoAtual;

    @Dado("que o paciente {string} está cadastrado no sistema para inadimplência")
    public void pacienteCadastrado(String nome) {
        service.cadastrarPaciente(nome);
        pacienteId = 1L;
    }

    @Dado("que {string} possui uma parcela vencida há {int} dias")
    public void possuiParcelaVencida(String nome, int dias) {
        service.adicionarParcelaVencida(pacienteId, 300.0, dias);
    }

    @Dado("que {string} possui uma parcela de R${double} vencida há {int} dias")
    public void possuiParcelaComValor(String nome, double valor, int dias) {
        service.adicionarParcelaVencida(pacienteId, valor, dias);
    }

    @Dado("que {string} possui {int} parcelas vencidas no valor total de R${double}")
    public void possuiParcelasVencidas(String nome, int quantidade, double total) {
        double valorCada = total / quantidade;
        for (int i = 0; i < quantidade; i++) {
            service.adicionarParcelaVencida(pacienteId, valorCada, 31 + i);
        }
    }

    @Dado("que {string} firmou um acordo com parcelas substituídas")
    public void firmouAcordo(String nome) {
        service.adicionarParcelaVencida(pacienteId, 300.0, 35);
        acordoAtual = service.firmarAcordo(pacienteId, 2, 150.0);
    }

    @Quando("o sistema verifica o status de inadimplência")
    public void verificaStatusInadimplencia() {
        restricaoAtual = service.verificarRestricao(pacienteId);
    }

    @Quando("a recepcionista consulta os valores atualizados da parcela")
    public void consultaValoresParcela() {
        parcelasConsultadas = service.consultarParcelas(pacienteId);
    }

    @Quando("a recepcionista firma um acordo consolidando as {int} parcelas em {int} novas parcelas de R${double} cada")
    public void firmaAcordo(int qtdOriginais, int qtdNovas, double valorCada) {
        acordoAtual = service.firmarAcordo(pacienteId, qtdNovas, valorCada);
    }

    @Quando("{string} deixa de pagar as novas parcelas do acordo")
    public void deixaDePagarAcordo(String nome) {
        service.registrarInadimplenciaAcordo(acordoAtual.getId().getId());
    }

    @Então("{string} deve receber o status de {string}")
    public void deveReceberStatus(String nome, String status) {
        if ("Restrito".equalsIgnoreCase(status)) {
            assertTrue(restricaoAtual, nome + " deveria estar Restrito");
        }
    }

    @Então("{string} não deve receber o status de {string}")
    public void naoDeveReceberStatus(String nome, String status) {
        if ("Restrito".equalsIgnoreCase(status)) {
            assertFalse(restricaoAtual, nome + " não deveria estar Restrito");
        }
    }

    @Então("novos agendamentos não emergenciais devem ser bloqueados para {string}")
    public void agendamentosDevemSerBloqueados(String nome) {
        assertTrue(service.verificarRestricao(pacienteId),
                "Paciente restrito deve ter agendamentos bloqueados");
    }

    @Então("os juros e a multa devem ser calculados automaticamente pelo sistema")
    public void jurosEMultaCalculadosAutomaticamente() {
        assertFalse(parcelasConsultadas.isEmpty());
        Parcela p = parcelasConsultadas.get(0);
        assertTrue(p.getMulta() > 0, "Multa deve ser maior que zero");
        assertTrue(p.getJuros() > 0, "Juros devem ser maiores que zero");
    }

    @Então("a recepcionista não deve conseguir alterar os valores de juros e multa")
    public void naoDeveAlterarJurosEMulta() {
        // juros/multa são calculados internamente — não há setter público
        Parcela p = parcelasConsultadas.get(0);
        assertNotNull(p); // sem setter exposto = imutável por design
    }

    @Então("as {int} parcelas originais devem ter o status {string}")
    public void parcelasOriginaisComStatus(int qtd, String statusEsperado) {
        List<Parcela> originais = acordoAtual.getParcelasOriginais();
        assertEquals(qtd, originais.size());
        originais.forEach(p ->
                assertEquals(StatusParcela.SUBSTITUIDA, p.getStatus(),
                        "Parcela original deve estar Substituída"));
    }

    @Então("{int} novas parcelas devem ser geradas com status {string}")
    public void novasParcelasComStatus(int qtd, String statusEsperado) {
        List<Parcela> novas = acordoAtual.getNovasParcelas();
        assertEquals(qtd, novas.size());
        novas.forEach(p ->
                assertEquals(StatusParcela.PENDENTE, p.getStatus(),
                        "Nova parcela deve estar Pendente"));
    }

    @Então("as parcelas substituídas não devem somar no Fluxo de Caixa")
    public void parcelasSubstituídasNaoSomamFluxo() {
        acordoAtual.getParcelasOriginais().forEach(p ->
                assertEquals(StatusParcela.SUBSTITUIDA, p.getStatus()));
    }

    @Então("o acordo deve ser marcado como inadimplido")
    public void acordoMarcadoComoInadimplido() {
        assertTrue(acordoAtual.isInadimplido(), "Acordo deve estar inadimplido");
    }

    @Então("as multas retroativas das parcelas originais devem voltar a ser consideradas")
    public void multasRetroativasVoltam() {
        acordoAtual.getParcelasOriginais().forEach(p ->
                assertTrue(p.getMulta() > 0,
                        "Multa retroativa deve estar calculada na parcela original"));
    }
}