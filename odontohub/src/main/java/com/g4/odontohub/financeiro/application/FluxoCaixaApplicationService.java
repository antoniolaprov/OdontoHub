package com.g4.odontohub.financeiro.application;

import com.g4.odontohub.financeiro.domain.event.AlertaFluxoNegativo;
import com.g4.odontohub.financeiro.domain.event.EntradaGeradaAutomaticamente;
import com.g4.odontohub.financeiro.domain.event.SaidaRegistradaManualmente;
import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import com.g4.odontohub.financeiro.domain.model.Parcela;
import com.g4.odontohub.financeiro.domain.repository.LancamentoRepository;
import com.g4.odontohub.financeiro.domain.service.FluxoCaixaService;
import com.g4.odontohub.financeiro.domain.service.ParcelaService;
import com.g4.odontohub.financeiro.infrastructure.persistence.InMemoryLancamentoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.List;

public class FluxoCaixaApplicationService {

    private final FluxoCaixaService fluxoCaixaService;
    private final ParcelaService parcelaService = new ParcelaService();
    private AlertaFluxoNegativo ultimoAlerta;
    private LancamentoFinanceiro ultimoLancamento;

    public FluxoCaixaApplicationService() {
        this(new InMemoryLancamentoRepository());
    }

    public FluxoCaixaApplicationService(LancamentoRepository lancamentoRepository) {
        this.fluxoCaixaService = new FluxoCaixaService(lancamentoRepository);
    }

    public Parcela criarParcela(double valor) {
        return parcelaService.criarParcela(valor, LocalDate.now().plusDays(30));
    }

    public LancamentoFinanceiro liquidarParcela(Parcela parcela) {
        List<Object> eventos = parcelaService.liquidar(parcela, LocalDate.now());
        LancamentoFinanceiro lancamento = null;
        for (Object evento : eventos) {
            if (evento instanceof EntradaGeradaAutomaticamente e) {
                lancamento = fluxoCaixaService.registrarEntrada(e.valor(), e.origem(), e.data(), true);
                ultimoLancamento = lancamento;
            }
            DomainEventPublisher.publish(evento);
        }
        return lancamento;
    }

    public LancamentoFinanceiro registrarEntradaAutomatica(double valor, String descricao, LocalDate data) {
        LancamentoFinanceiro l = fluxoCaixaService.registrarEntrada(valor, descricao, data, true);
        ultimoLancamento = l;
        DomainEventPublisher.publish(new EntradaGeradaAutomaticamente(l.getId(), valor, data, descricao));
        return l;
    }

    public LancamentoFinanceiro registrarSaidaManual(double valor, String categoria, String descricao) {
        LancamentoFinanceiro l = fluxoCaixaService.registrarSaida(valor, categoria, descricao, false);
        ultimoLancamento = l;
        DomainEventPublisher.publish(new SaidaRegistradaManualmente(l.getId(), valor, categoria));
        return l;
    }

    public LancamentoFinanceiro registrarSaidaAutomatica(double valor, String categoria, String descricao) {
        LancamentoFinanceiro l = fluxoCaixaService.registrarSaida(valor, categoria, descricao, true);
        ultimoLancamento = l;
        return l;
    }

    /** Entrada futura prevista (parcela a vencer) que alimenta a projeção de saldo. */
    public LancamentoFinanceiro registrarEntradaPrevista(double valor, String descricao, LocalDate vencimento) {
        LancamentoFinanceiro l = fluxoCaixaService.registrarEntradaPrevista(valor, descricao, vencimento);
        ultimoLancamento = l;
        return l;
    }

    /** Saída futura prevista (despesa planejada) que alimenta a projeção de saldo. */
    public LancamentoFinanceiro registrarSaidaPrevista(double valor, String categoria, String descricao, LocalDate data) {
        LancamentoFinanceiro l = fluxoCaixaService.registrarSaidaPrevista(valor, categoria, descricao, data);
        ultimoLancamento = l;
        return l;
    }

    /** Saldo atual: apenas lançamentos confirmados. */
    public double calcularSaldoAtual() {
        return fluxoCaixaService.calcularSaldoAtual();
    }

    /**
     * Saldo projetado (entradas confirmadas + parcelas a vencer − saídas previstas).
     * Emite alerta de fluxo negativo quando a projeção fica negativa.
     */
    public double calcularSaldoProjetado() {
        double saldo = fluxoCaixaService.calcularSaldoProjetado();
        if (saldo < 0) {
            ultimoAlerta = new AlertaFluxoNegativo(saldo, LocalDate.now());
            DomainEventPublisher.publish(ultimoAlerta);
        }
        return saldo;
    }

    public int calcularPontoDeEquilibrio(double custoFixo, double valorMedio) {
        return fluxoCaixaService.calcularPontoDeEquilibrio(custoFixo, valorMedio);
    }

    public LancamentoFinanceiro getUltimoLancamento() { return ultimoLancamento; }
    public AlertaFluxoNegativo getUltimoAlerta() { return ultimoAlerta; }
    public List<LancamentoFinanceiro> getLancamentos() { return fluxoCaixaService.getLancamentos(); }
}
