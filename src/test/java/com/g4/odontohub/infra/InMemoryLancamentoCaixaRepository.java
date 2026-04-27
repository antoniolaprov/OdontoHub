package com.g4.odontohub.infra;

import com.g4.odontohub.fluxocaixa.domain.LancamentoCaixa;
import com.g4.odontohub.fluxocaixa.domain.LancamentoCaixaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryLancamentoCaixaRepository implements LancamentoCaixaRepository {

    private final Map<String, LancamentoCaixa> db = new HashMap<>();

    @Override
    public LancamentoCaixa salvar(LancamentoCaixa lancamento) {
        db.put(lancamento.getId(), lancamento);
        return lancamento;
    }

    @Override
    public Optional<LancamentoCaixa> buscarPorId(String id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public List<LancamentoCaixa> listarTodos() {
        return new ArrayList<>(db.values());
    }

    @Override
    public List<LancamentoCaixa> listarAteData(LocalDate data) {
        List<LancamentoCaixa> result = new ArrayList<>();
        for (LancamentoCaixa l : db.values()) {
            if (!l.getData().isAfter(data)) {
                result.add(l);
            }
        }
        return result;
    }

    @Override
    public BigDecimal somarEntradasPrevisasTe(LocalDate data) {
        return db.values().stream()
                .filter(l -> !l.getData().isAfter(data))
                .map(LancamentoCaixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal somarSaidasPrevistas(LocalDate data) {
        return db.values().stream()
                .filter(l -> !l.getData().isAfter(data))
                .map(LancamentoCaixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
