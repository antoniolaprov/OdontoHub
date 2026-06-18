package com.g4.odontohub.financeiro.domain.service;

import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import com.g4.odontohub.financeiro.domain.model.LancamentoId;
import com.g4.odontohub.financeiro.domain.model.TipoLancamento;
import com.g4.odontohub.financeiro.domain.repository.LancamentoRepository;

import java.time.LocalDate;
import java.util.List;

public class FluxoCaixaService {

    private final LancamentoRepository repositorio;

    public FluxoCaixaService(LancamentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public LancamentoFinanceiro registrarEntrada(double valor, String descricao, LocalDate data, boolean automatico) {
        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(repositorio.proximoId()), TipoLancamento.ENTRADA,
                valor, data, "Pagamento", descricao, automatico);
        repositorio.salvar(l);
        return l;
    }

    public LancamentoFinanceiro registrarSaida(double valor, String categoria, String descricao, boolean automatico) {
        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(repositorio.proximoId()), TipoLancamento.SAIDA,
                valor, LocalDate.now(), categoria, descricao, automatico);
        repositorio.salvar(l);
        return l;
    }

    /** Entrada futura prevista (ex.: parcela com vencimento a vencer). */
    public LancamentoFinanceiro registrarEntradaPrevista(double valor, String descricao, LocalDate vencimento) {
        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(repositorio.proximoId()), TipoLancamento.ENTRADA,
                valor, vencimento, "Parcela a vencer", descricao, false, true);
        repositorio.salvar(l);
        return l;
    }

    /** Saída futura prevista (ex.: despesa planejada do mês). */
    public LancamentoFinanceiro registrarSaidaPrevista(double valor, String categoria, String descricao, LocalDate data) {
        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(repositorio.proximoId()), TipoLancamento.SAIDA,
                valor, data, categoria, descricao, false, true);
        repositorio.salvar(l);
        return l;
    }

    /** Saldo atual: apenas lançamentos confirmados (entradas confirmadas − saídas realizadas). */
    public double calcularSaldoAtual() {
        return saldo(false);
    }

    /**
     * Saldo projetado: saldo atual + parcelas com vencimento futuro − saídas previstas
     * (considera lançamentos confirmados e previstos).
     */
    public double calcularSaldoProjetado() {
        return saldo(true);
    }

    private double saldo(boolean incluirPrevistos) {
        double entradas = repositorio.todos().stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA)
                .filter(l -> incluirPrevistos || !l.isPrevisto())
                .mapToDouble(LancamentoFinanceiro::getValor).sum();
        double saidas = repositorio.todos().stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA)
                .filter(l -> incluirPrevistos || !l.isPrevisto())
                .mapToDouble(LancamentoFinanceiro::getValor).sum();
        return entradas - saidas;
    }

    public int calcularPontoDeEquilibrio(double custoFixo, double valorMedio) {
        return (int) Math.ceil(custoFixo / valorMedio);
    }

    public List<LancamentoFinanceiro> getLancamentos() {
        return repositorio.todos();
    }
}
