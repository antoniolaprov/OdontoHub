package com.g4.odontohub.financeiro.application;

import com.g4.odontohub.financeiro.domain.event.AlertaFluxoNegativo;
import com.g4.odontohub.financeiro.domain.event.EntradaGeradaAutomaticamente;
import com.g4.odontohub.financeiro.domain.event.SaidaRegistradaManualmente;
import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import com.g4.odontohub.financeiro.domain.model.Parcela;
import com.g4.odontohub.financeiro.domain.service.FluxoCaixaService;
import com.g4.odontohub.financeiro.domain.service.ParcelaService;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.List;

public class FluxoCaixaApplicationService {

    private final FluxoCaixaService fluxoCaixaService = new FluxoCaixaService();
    private final ParcelaService parcelaService = new ParcelaService();
    private AlertaFluxoNegativo ultimoAlerta;
    private LancamentoFinanceiro ultimoLancamento;

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

    public double calcularSaldoProjetado() {
        double saldo = fluxoCaixaService.calcularSaldo();
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
