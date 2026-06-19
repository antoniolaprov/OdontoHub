package com.g4.odontohub.pagamento.application;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.pagamento.domain.event.PagamentoRegistrado;
import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;
import com.g4.odontohub.pagamento.domain.repository.PagamentoRepository;
import com.g4.odontohub.pagamento.domain.service.PagamentoService;
import com.g4.odontohub.pagamento.infrastructure.persistence.InMemoryPagamentoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;

public class PagamentoApplicationService {

    private final PagamentoService service;

    public PagamentoApplicationService() {
        this(new InMemoryPagamentoRepository());
    }

    public PagamentoApplicationService(PagamentoRepository repositorio) {
        this.service = new PagamentoService(repositorio);
    }

    /**
     * Composition root de produção: além de persistir o pagamento, integra com o
     * contexto Financeiro lançando uma entrada real no fluxo de caixa a cada
     * {@link PagamentoRegistrado} (mesmo padrão de Estoque → Financeiro).
     */
    public PagamentoApplicationService(PagamentoRepository repositorio,
                                       FluxoCaixaApplicationService fluxoCaixaService) {
        this.service = new PagamentoService(repositorio);
        DomainEventPublisher.subscribe(PagamentoRegistrado.class, evento ->
                fluxoCaixaService.registrarEntradaAutomatica(
                        evento.valorRecebido(),
                        "Recebimento da parcela " + evento.parcelaReferencia(),
                        LocalDate.now()));
    }

    public ParcelaPagavel criarParcela(String referencia, double valorDevido) {
        return service.criarParcela(referencia, valorDevido);
    }

    public Pagamento registrarPagamentoPresencial(String referencia, double valor, LocalDate data, String forma) {
        return service.registrarPagamentoPresencial(referencia, valor, data, forma);
    }

    public Pagamento registrarPagamentoPresencial(String referencia, double valor, LocalDate data,
                                                  String forma, String observacao) {
        return service.registrarPagamentoPresencial(referencia, valor, data, forma, observacao);
    }

    public Pagamento lancarAguardandoComprovante(String referencia, String forma) {
        return service.lancarAguardandoComprovante(referencia, forma);
    }

    public Pagamento confirmarPagamento(String referencia, double valor, LocalDate data) {
        return service.confirmarPagamento(referencia, valor, data);
    }

    public void cancelarLancamentoAguardando(String referencia, String justificativa) {
        service.cancelarLancamentoAguardando(referencia, justificativa);
    }

    public boolean comprovanteDisponivel(String referencia) {
        return service.comprovanteDisponivel(referencia);
    }

    public boolean declaracaoQuitacaoDisponivel() {
        return service.declaracaoQuitacaoDisponivel();
    }

    public ParcelaPagavel parcela(String referencia) {
        return service.parcela(referencia);
    }

    public int getQuantidadeEntradasFluxoCaixa() {
        return service.getQuantidadeEntradasFluxoCaixa();
    }

    public double getTotalEntradasFluxoCaixa() {
        return service.getTotalEntradasFluxoCaixa();
    }

    public java.util.List<Pagamento> pagamentos() {
        return service.pagamentos();
    }
}
