package com.g4.odontohub.pagamento.domain.service;

import com.g4.odontohub.pagamento.domain.event.LancamentoAguardandoCriado;
import com.g4.odontohub.pagamento.domain.event.PagamentoCancelado;
import com.g4.odontohub.pagamento.domain.event.PagamentoRegistrado;
import com.g4.odontohub.pagamento.domain.model.FormaPagamento;
import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.PagamentoId;
import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;
import com.g4.odontohub.pagamento.domain.model.StatusPagamento;
import com.g4.odontohub.pagamento.domain.repository.PagamentoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * F17 — Gestão de Pagamentos e Quitação de Débitos.
 * Registra pagamentos presenciais e remotos, baixa parcelas e alimenta o fluxo de caixa.
 * A persistência é delegada à porta {@link PagamentoRepository} (camada de infraestrutura).
 */
public class PagamentoService {

    private final PagamentoRepository repositorio;
    private final List<Double> entradasFluxoCaixa = new ArrayList<>();
    private long nextId = 1;

    public PagamentoService(PagamentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public ParcelaPagavel criarParcela(String referencia, double valorDevido) {
        ParcelaPagavel parcela = new ParcelaPagavel(referencia, valorDevido);
        repositorio.salvarParcela(parcela);
        return parcela;
    }

    public Pagamento registrarPagamentoPresencial(String referencia, double valor, LocalDate data, String formaLabel) {
        FormaPagamento forma = FormaPagamento.fromLabel(formaLabel);
        validarValorEData(valor, data);
        ParcelaPagavel parcela = parcela(referencia);
        if (parcela.estaQuitada()) {
            throw new IllegalStateException("Parcela já quitada não permite novo registro");
        }
        Pagamento pagamento = new Pagamento(new PagamentoId(nextId++), referencia, forma, StatusPagamento.PAGO);
        pagamento.confirmar(valor, data);
        repositorio.salvarPagamento(pagamento);
        parcela.registrarPagamento(valor);
        entradasFluxoCaixa.add(valor);
        DomainEventPublisher.publish(new PagamentoRegistrado(pagamento.getId(), referencia, valor));
        return pagamento;
    }

    public Pagamento lancarAguardandoComprovante(String referencia, String formaLabel) {
        FormaPagamento forma = FormaPagamento.fromLabel(formaLabel);
        if (existeAguardando(referencia)) {
            throw new IllegalStateException("Já existe um lançamento aguardando comprovante para esta parcela");
        }
        parcela(referencia); // garante que a parcela existe
        Pagamento pagamento = new Pagamento(new PagamentoId(nextId++), referencia, forma,
                StatusPagamento.AGUARDANDO_COMPROVANTE);
        repositorio.salvarPagamento(pagamento);
        DomainEventPublisher.publish(new LancamentoAguardandoCriado(pagamento.getId(), referencia));
        return pagamento;
    }

    public Pagamento confirmarPagamento(String referencia, double valor, LocalDate data) {
        validarValorEData(valor, data);
        Pagamento pagamento = aguardandoDe(referencia)
                .orElseThrow(() -> new IllegalStateException("Sem lançamento aguardando para a parcela " + referencia));
        pagamento.confirmar(valor, data);
        parcela(referencia).registrarPagamento(valor);
        entradasFluxoCaixa.add(valor);
        DomainEventPublisher.publish(new PagamentoRegistrado(pagamento.getId(), referencia, valor));
        return pagamento;
    }

    public void cancelarLancamentoAguardando(String referencia, String justificativa) {
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("A justificativa do cancelamento é obrigatória");
        }
        Pagamento pagamento = aguardandoDe(referencia)
                .orElseThrow(() -> new IllegalStateException("Sem lançamento aguardando para a parcela " + referencia));
        pagamento.cancelar(justificativa);
        DomainEventPublisher.publish(new PagamentoCancelado(pagamento.getId(), justificativa));
    }

    public boolean comprovanteDisponivel(String referencia) {
        return repositorio.todosPagamentos().stream()
                .anyMatch(p -> p.getParcelaReferencia().equals(referencia) && p.estaPago());
    }

    public boolean declaracaoQuitacaoDisponivel() {
        return !repositorio.todasParcelas().isEmpty()
                && repositorio.todasParcelas().stream().allMatch(ParcelaPagavel::estaQuitada);
    }

    public ParcelaPagavel parcela(String referencia) {
        ParcelaPagavel parcela = repositorio.buscarParcela(referencia);
        if (parcela == null) {
            throw new IllegalArgumentException("Parcela não encontrada: " + referencia);
        }
        return parcela;
    }

    public int getQuantidadeEntradasFluxoCaixa() {
        return entradasFluxoCaixa.size();
    }

    public double getTotalEntradasFluxoCaixa() {
        return entradasFluxoCaixa.stream().mapToDouble(Double::doubleValue).sum();
    }

    private boolean existeAguardando(String referencia) {
        return aguardandoDe(referencia).isPresent();
    }

    private Optional<Pagamento> aguardandoDe(String referencia) {
        return repositorio.todosPagamentos().stream()
                .filter(p -> p.getParcelaReferencia().equals(referencia) && p.estaAguardando())
                .findFirst();
    }

    private void validarValorEData(double valor, LocalDate data) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser positivo");
        }
        if (data != null && data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data do pagamento não pode ser futura");
        }
    }
}
