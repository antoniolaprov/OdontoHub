package com.g4.odontohub.infra;

import com.g4.odontohub.fluxocaixa.application.ParcelaVencimentoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InMemoryParcelaVencimentoRepository implements ParcelaVencimentoRepository {

    private final List<Parcela> parcelas = new ArrayList<>();

    @Override
    public BigDecimal somarParcelasAVencer(LocalDate dataLimite) {
        return parcelas.stream()
                .filter(p -> !p.vencimento.isAfter(dataLimite))
                .map(p -> p.valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void adicionarParcela(LocalDate vencimento, BigDecimal valor) {
        parcelas.add(new Parcela(vencimento, valor));
    }

    private static class Parcela {
        final LocalDate vencimento;
        final BigDecimal valor;
        Parcela(LocalDate vencimento, BigDecimal valor) {
            this.vencimento = vencimento;
            this.valor = valor;
        }
    }
}
