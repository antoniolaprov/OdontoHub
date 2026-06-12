package com.g4.odontohub.financeiro.infrastructure.persistence;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryInadimplenciaRepository implements InadimplenciaRepository {

    private final Map<String, List<ParcelaCobranca>> vencidas = new HashMap<>();
    private final Map<String, Acordo> acordos = new HashMap<>();
    private long acordoSeq = 0;

    @Override
    public void salvarVencida(String paciente, ParcelaCobranca parcela) {
        vencidas.computeIfAbsent(paciente, p -> new ArrayList<>()).add(parcela);
    }

    @Override
    public List<ParcelaCobranca> vencidasDe(String paciente) {
        return new ArrayList<>(vencidas.getOrDefault(paciente, new ArrayList<>()));
    }

    @Override
    public void salvarAcordo(String paciente, Acordo acordo) {
        acordos.put(paciente, acordo);
    }

    @Override
    public Acordo acordoDe(String paciente) {
        return acordos.get(paciente);
    }

    @Override
    public long proximoAcordoId() {
        return ++acordoSeq;
    }
}
