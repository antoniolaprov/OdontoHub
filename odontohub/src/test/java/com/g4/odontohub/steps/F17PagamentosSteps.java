package com.g4.odontohub.steps;

import com.g4.odontohub.pagamento.application.PagamentoApplicationService;
import com.g4.odontohub.pagamento.domain.model.StatusParcelaPagamento;
import io.cucumber.java.pt.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class F17PagamentosSteps {

    private final PagamentoApplicationService service = new PagamentoApplicationService();
    private Exception excecaoCapturada;

    @Dado("que existe uma parcela {string} no valor de {dinheiro} a quitar")
    public void existeParcela(String referencia, double valor) {
        service.criarParcela(referencia, valor);
    }

    @Dado("que existe um lançamento aguardando comprovante na forma {string} para a parcela {string}")
    public void existeLancamentoAguardando(String forma, String referencia) {
        service.lancarAguardandoComprovante(referencia, forma);
    }

    @Quando("a recepcionista registra o pagamento presencial de {dinheiro} na forma {string} para a parcela {string}")
    public void registraPagamentoPresencial(double valor, String forma, String referencia) {
        service.registrarPagamentoPresencial(referencia, valor, LocalDate.now(), forma);
    }

    @Quando("a recepcionista tenta registrar um pagamento com data futura para a parcela {string}")
    public void tentaPagamentoComDataFutura(String referencia) {
        tentar(() -> service.registrarPagamentoPresencial(referencia, 500.0, LocalDate.now().plusDays(5), "PIX"));
    }

    @Quando("a recepcionista tenta registrar um pagamento de {dinheiro} para a parcela {string}")
    public void tentaPagamentoComValor(double valor, String referencia) {
        tentar(() -> service.registrarPagamentoPresencial(referencia, valor, LocalDate.now(), "PIX"));
    }

    @Quando("a recepcionista lança um pagamento aguardando comprovante na forma {string} para a parcela {string}")
    public void lancaAguardando(String forma, String referencia) {
        service.lancarAguardandoComprovante(referencia, forma);
    }

    @Quando("a recepcionista confirma o pagamento de {dinheiro} na data de hoje para a parcela {string}")
    public void confirmaPagamento(double valor, String referencia) {
        service.confirmarPagamento(referencia, valor, LocalDate.now());
    }

    @Quando("a recepcionista tenta lançar outro pagamento aguardando comprovante para a parcela {string}")
    public void tentaLancarOutroAguardando(String referencia) {
        tentar(() -> service.lancarAguardandoComprovante(referencia, "PIX"));
    }

    @Então("a parcela {string} deve ficar com status {string}")
    public void parcelaComStatus(String referencia, String status) {
        assertEquals(StatusParcelaPagamento.valueOf(status.toUpperCase()),
                service.parcela(referencia).getStatus());
    }

    @E("deve ser gerada uma entrada no fluxo de caixa de {dinheiro}")
    public void deveSerGeradaEntradaFluxo(double valor) {
        assertTrue(service.getQuantidadeEntradasFluxoCaixa() >= 1,
                "Deveria ter sido gerada uma entrada no fluxo de caixa");
    }

    @E("o saldo remanescente da parcela {string} deve ser {dinheiro}")
    public void saldoRemanescente(String referencia, double valor) {
        assertEquals(valor, service.parcela(referencia).getSaldoRemanescente(), 0.001);
    }

    @Então("o sistema deve rejeitar o pagamento")
    public void sistemaRejeitaPagamento() {
        assertNotNull(excecaoCapturada, "O sistema deveria ter rejeitado o pagamento");
    }

    @E("nenhuma entrada deve ser gerada no fluxo de caixa")
    public void nenhumaEntradaGerada() {
        assertEquals(0, service.getQuantidadeEntradasFluxoCaixa());
    }

    @Então("deve ser possível gerar o comprovante do pagamento da parcela {string}")
    public void devePoderGerarComprovante(String referencia) {
        assertTrue(service.comprovanteDisponivel(referencia));
    }

    @Então("a declaração de quitação total deve estar disponível")
    public void declaracaoQuitacaoDisponivel() {
        assertTrue(service.declaracaoQuitacaoDisponivel());
    }

    private void tentar(Runnable acao) {
        try {
            acao.run();
        } catch (Exception e) {
            excecaoCapturada = e;
            SharedTestServices.setLastException(e);
        }
    }
}
