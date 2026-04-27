package com.g4.odontohub.infra;

import com.g4.odontohub.manutencao.domain.Equipamento;
import com.g4.odontohub.manutencao.domain.EquipamentoRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryEquipamentoRepository implements EquipamentoRepository {

    private final Map<String, Equipamento> banco = new HashMap<>();

    @Override
    public void salvar(Equipamento equipamento) {
        banco.put(equipamento.getNome(), equipamento);
    }

    @Override
    public Equipamento buscarPorNome(String nome) {
        return banco.get(nome);
    }
}