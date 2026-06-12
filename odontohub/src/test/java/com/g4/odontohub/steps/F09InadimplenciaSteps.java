package com.g4.odontohub.steps;

import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.model.StatusParcela;
import io.cucumber.java.pt.*;

import static org.junit.jupiter.api.Assertions.*;

public class F09InadimplenciaSteps {

    private final InadimplenciaApplicationService service = new InadimplenciaApplicationService();
    private String ultimoPaciente;
    private ParcelaCobranca ultimaParcela;
    private Acordo acordo;

    @Dado("que {string} possui uma parcela vencida há {int} dias")
    public void parcelaVencidaHaDias(String nome, int dias) {
        ultimoPaciente = nome;
        service.registrarParcelaVencida(nome, 400.0, dias);
    }

    @Dado("que {string} possui uma parcela de {dinheiro} vencida há {int} dias")
    public void parcelaDeValorVencidaHaDias(String nome, double valor, int dias) {
        ultimoPaciente = nome;
        ultimaParcela = service.registrarParcelaVencida(nome, valor, dias);
    }

    @Dado("que {string} possui {int} parcelas vencidas no valor total de {dinheiro}")
    public void possuiParcelasVencidas(String nome, int quantidade, double valorTotal) {
        ultimoPaciente = nome;
        service.registrarParcelasVencidas(nome, quantidade, valorTotal);
    }

    @Dado("que {string} firmou um acordo com parcelas substituídas")
    public void firmouAcordoComSubstituidas(String nome) {
        ultimoPaciente = nome;
        service.registrarParcelasVencidas(nome, 3, 900.0);
        acordo = service.firmarAcordo(nome, 2, 450.0);
    }

    @Quando("o sistema verifica o status de inadimplência")
    public void verificaStatusInadimplencia() {
        // Status é avaliado sob demanda nas asserções seguintes.
    }

    @Quando("a recepcionista consulta os valores atualizados da parcela")
    public void consultaValoresParcela() {
        ultimaParcela = service.consultarParcela(ultimoPaciente);
    }

    @Quando("a recepcionista firma um acordo consolidando as {int} parcelas em {int} novas parcelas de {dinheiro} cada")
    public void firmaAcordoConsolidando(int qtdOriginais, int qtdNovas, double valorNova) {
        acordo = service.firmarAcordo(ultimoPaciente, qtdNovas, valorNova);
    }

    @Quando("{string} deixa de pagar as novas parcelas do acordo")
    public void deixaDePagarNovasParcelas(String nome) {
        service.inadimplirAcordo(nome);
    }

    @Então("{string} deve receber o status de {string}")
    public void deveReceberStatus(String nome, String status) {
        assertTrue(service.verificarRestricao(nome), nome + " deveria estar " + status);
    }

    @Então("{string} não deve receber o status de {string}")
    public void naoDeveReceberStatus(String nome, String status) {
        assertFalse(service.verificarRestricao(nome), nome + " não deveria estar " + status);
    }

    @E("novos agendamentos não emergenciais devem ser bloqueados para {string}")
    public void agendamentosBloqueados(String nome) {
        assertTrue(service.agendamentoBloqueado(nome));
    }

    @Então("os juros e a multa devem ser calculados automaticamente pelo sistema")
    public void jurosEMultaCalculados() {
        assertNotNull(ultimaParcela);
        assertTrue(ultimaParcela.getJuros() > 0, "Juros deveriam ser calculados");
        assertTrue(ultimaParcela.getMulta() > 0, "Multa deveria ser calculada");
    }

    @E("a recepcionista não deve conseguir alterar os valores de juros e multa")
    public void naoPodeAlterarJurosMulta() {
        assertThrows(IllegalStateException.class, () -> ultimaParcela.editarJurosOuMulta(0.0));
    }

    @Então("as {int} parcelas originais devem ter o status {string}")
    public void parcelasOriginaisComStatus(int quantidade, String status) {
        assertEquals(quantidade, acordo.getParcelasOriginais().size());
        assertTrue(acordo.getParcelasOriginais().stream()
                .allMatch(p -> p.getStatus() == StatusParcela.SUBSTITUIDA));
    }

    @E("{int} novas parcelas devem ser geradas com status {string}")
    public void novasParcelasComStatus(int quantidade, String status) {
        assertEquals(quantidade, acordo.getNovasParcelas().size());
        assertTrue(acordo.getNovasParcelas().stream()
                .allMatch(p -> p.getStatus() == StatusParcela.PENDENTE));
    }

    @E("as parcelas substituídas não devem somar no Fluxo de Caixa")
    public void substituidasNaoSomamNoFluxo() {
        assertEquals(0.0, service.totalSubstituidasNoFluxoCaixa(ultimoPaciente), 0.001);
    }

    @Então("o acordo deve ser marcado como inadimplido")
    public void acordoInadimplido() {
        assertTrue(service.acordoDe(ultimoPaciente).isInadimplido());
    }

    @E("as multas retroativas das parcelas originais devem voltar a ser consideradas")
    public void multasRetroativasVoltam() {
        assertTrue(service.acordoDe(ultimoPaciente).isMultasRetroativasAtivas());
    }
}
