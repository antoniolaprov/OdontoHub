package com.g4.odontohub.financeiro.domain.service;

import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import com.g4.odontohub.financeiro.domain.model.LancamentoId;
import com.g4.odontohub.financeiro.domain.model.TipoLancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FluxoCaixaService {

    private final List<LancamentoFinanceiro> lancamentos = new ArrayList<>();
    private long nextId = 1L;

    public LancamentoFinanceiro registrarEntrada(double valor, String descricao, LocalDate data, boolean automatico) {
        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(nextId++), TipoLancamento.ENTRADA,
                valor, data, "Pagamento", descricao, automatico);
        lancamentos.add(l);
        return l;
    }

    public LancamentoFinanceiro registrarSaida(double valor, String categoria, String descricao, boolean automatico) {
        LancamentoFinanceiro l = new LancamentoFinanceiro(
                new LancamentoId(nextId++), TipoLancamento.SAIDA,
                valor, LocalDate.now(), categoria, descricao, automatico);
        lancamentos.add(l);
        return l;
    }

    public double calcularSaldo() {
        double entradas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoLancamento.ENTRADA)
                .mapToDouble(LancamentoFinanceiro::getValor).sum();
        double saidas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA)
                .mapToDouble(LancamentoFinanceiro::getValor).sum();
        return entradas - saidas;
    }

    public int calcularPontoDeEquilibrio(double custoFixo, double valorMedio) {
        return (int) Math.ceil(custoFixo / valorMedio);
    }

    public List<LancamentoFinanceiro> getLancamentos() {
        return Collections.unmodifiableList(lancamentos);
    }
}
