package com.g4.odontohub.financeiro.domain.service;

import com.g4.odontohub.financeiro.domain.event.AcordoFirmado;
import com.g4.odontohub.financeiro.domain.event.AcordoInadimplido;
import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.AcordoId;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * F09 — Gestão Ativa de Inadimplência e Acordos.
 * Juros e multa são calculados automaticamente com base na política da clínica.
 */
public class InadimplenciaService {

    private static final int DIAS_PARA_RESTRICAO = 30;
    private static final double PERCENTUAL_MULTA = 0.02;      // 2% sobre o valor
    private static final double PERCENTUAL_JUROS_DIA = 0.0033; // ~0,33% ao dia

    private final Map<String, List<ParcelaCobranca>> parcelasPorPaciente = new HashMap<>();
    private final Map<String, Acordo> acordoPorPaciente = new HashMap<>();
    private long nextAcordoId = 1;

    public ParcelaCobranca registrarParcelaVencida(String paciente, double valor, int diasAtraso) {
        double multa = valor * PERCENTUAL_MULTA;
        double juros = valor * PERCENTUAL_JUROS_DIA * diasAtraso;
        ParcelaCobranca parcela = new ParcelaCobranca(valor, diasAtraso, multa, juros);
        parcelasPorPaciente.computeIfAbsent(paciente, p -> new ArrayList<>()).add(parcela);
        return parcela;
    }

    public void registrarParcelasVencidas(String paciente, int quantidade, double valorTotal) {
        double valorCada = valorTotal / quantidade;
        for (int i = 0; i < quantidade; i++) {
            registrarParcelaVencida(paciente, valorCada, 31);
        }
    }

    public boolean verificarRestricao(String paciente) {
        return parcelas(paciente).stream().anyMatch(p -> p.getDiasAtraso() > DIAS_PARA_RESTRICAO);
    }

    public boolean agendamentoBloqueado(String paciente) {
        return verificarRestricao(paciente);
    }

    public ParcelaCobranca consultarParcela(String paciente) {
        return parcelas(paciente).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sem parcelas para: " + paciente));
    }

    public Acordo firmarAcordo(String paciente, int quantidadeNovas, double valorNova) {
        List<ParcelaCobranca> originais = new ArrayList<>(parcelas(paciente));
        List<ParcelaCobranca> novas = new ArrayList<>();
        for (int i = 0; i < quantidadeNovas; i++) {
            ParcelaCobranca nova = new ParcelaCobranca(valorNova, 0, 0, 0);
            nova.marcarPendente();
            novas.add(nova);
        }
        Acordo acordo = new Acordo(new AcordoId(nextAcordoId++), paciente, originais, novas);
        acordoPorPaciente.put(paciente, acordo);
        DomainEventPublisher.publish(new AcordoFirmado(acordo.getId(), originais.size(), novas.size()));
        return acordo;
    }

    public void inadimplirAcordo(String paciente) {
        Acordo acordo = acordoDe(paciente);
        acordo.inadimplir();
        DomainEventPublisher.publish(new AcordoInadimplido(acordo.getId()));
    }

    public Acordo acordoDe(String paciente) {
        Acordo acordo = acordoPorPaciente.get(paciente);
        if (acordo == null) {
            throw new IllegalArgumentException("Sem acordo para: " + paciente);
        }
        return acordo;
    }

    /** Soma das parcelas substituídas que ainda contam no fluxo de caixa (deve ser zero). */
    public double totalSubstituidasNoFluxoCaixa(String paciente) {
        Acordo acordo = acordoDe(paciente);
        return acordo.getParcelasOriginais().stream()
                .filter(ParcelaCobranca::contaNoFluxoCaixa)
                .mapToDouble(ParcelaCobranca::getValor)
                .sum();
    }

    private List<ParcelaCobranca> parcelas(String paciente) {
        return parcelasPorPaciente.getOrDefault(paciente, new ArrayList<>());
    }
}
