package com.g4.odontohub.pagamento.application;

import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;
import com.g4.odontohub.pagamento.domain.repository.PagamentoRepository;
import com.g4.odontohub.pagamento.domain.service.PagamentoService;
import com.g4.odontohub.pagamento.infrastructure.persistence.InMemoryPagamentoRepository;

import java.time.LocalDate;

public class PagamentoApplicationService {

    private final PagamentoService service;

    public PagamentoApplicationService() {
        this(new InMemoryPagamentoRepository());
    }

    public PagamentoApplicationService(PagamentoRepository repositorio) {
        this.service = new PagamentoService(repositorio);
    }

    public ParcelaPagavel criarParcela(String referencia, double valorDevido) {
        return service.criarParcela(referencia, valorDevido);
    }

    public Pagamento registrarPagamentoPresencial(String referencia, double valor, LocalDate data, String forma) {
        return service.registrarPagamentoPresencial(referencia, valor, data, forma);
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
}
