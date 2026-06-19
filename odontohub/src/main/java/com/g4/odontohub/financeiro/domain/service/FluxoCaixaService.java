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

    public LancamentoFinanceiro registrarEntrada(
            double valor,
            String descricao,
            LocalDate data,
            boolean automatico) {

        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(repositorio.proximoId()),
                TipoLancamento.ENTRADA,
                valor,
                data,
                "Pagamento",
                descricao,
                automatico);

        repositorio.salvar(l);
        return l;
    }

    public LancamentoFinanceiro registrarSaida(
            double valor,
            String categoria,
            String descricao,
            boolean automatico) {

        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(repositorio.proximoId()),
                TipoLancamento.SAIDA,
                valor,
                LocalDate.now(),
                categoria,
                descricao,
                automatico);

        repositorio.salvar(l);
        return l;
    }

    // =========================
    // MÉTODOS NOVOS
    // =========================

    public LancamentoFinanceiro registrarEntradaPrevista(
            double valor,
            String descricao,
            LocalDate vencimento) {

        return registrarEntrada(valor, descricao, vencimento, true);
    }

    public LancamentoFinanceiro registrarSaidaPrevista(
            double valor,
            String categoria,
            String descricao,
            LocalDate data) {

        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(repositorio.proximoId()),
                TipoLancamento.SAIDA,
                valor,
                data,
                categoria,
                descricao,
                true);

        repositorio.salvar(l);
        return l;
    }

    public double calcularSaldoAtual() {
        return calcularSaldo();
    }

    public double calcularSaldoProjetado() {
        return calcularSaldo();
    }

    // =========================

    public double calcularSaldo() {
        double entradas = repositorio.todos().stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA)
                .mapToDouble(LancamentoFinanceiro::getValor)
                .sum();

        double saidas = repositorio.todos().stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA)
                .mapToDouble(LancamentoFinanceiro::getValor)
                .sum();

        return entradas - saidas;
    }

    public int calcularPontoDeEquilibrio(double custoFixo, double valorMedio) {
        return (int) Math.ceil(custoFixo / valorMedio);
    }

    public List<LancamentoFinanceiro> getLancamentos() {
        return repositorio.todos();
    }
}