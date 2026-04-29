package com.g4.odontohub.financeiro.domain.service;

import com.g4.odontohub.financeiro.domain.model.Parcela;
import com.g4.odontohub.financeiro.domain.model.ParcelaId;
import com.g4.odontohub.financeiro.domain.model.ParcelaplanoId;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParcelaService {

    private final Map<ParcelaId, Parcela> parcelas = new HashMap<>();
    private long nextId = 1L;

    public Parcela criarParcela(double valor, LocalDate dataVencimento) {
        Parcela p = new Parcela(new ParcelaId(nextId++), new ParcelaplanoId(1L), valor, dataVencimento);
        parcelas.put(p.getId(), p);
        return p;
    }

    public List<Object> liquidar(Parcela parcela, LocalDate dataPagamento) {
        return parcela.liquidar(dataPagamento);
    }

    public boolean pacienteInadimplente(Long pacienteId) {
        return false;
    }
}
