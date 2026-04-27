package com.g4.odontohub.infra;

import com.g4.odontohub.manutencao.domain.RegistroManutencao;
import com.g4.odontohub.manutencao.domain.RegistroManutencaoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryRegistroManutencaoRepository implements RegistroManutencaoRepository {

    private final List<RegistroManutencao> banco = new ArrayList<>();

    @Override
    public void salvar(RegistroManutencao registro) {
        banco.add(registro);
    }

    @Override
    public List<RegistroManutencao> buscarPorEquipamento(UUID equipamentoId) {
        return banco.stream()
                .filter(r -> r.getEquipamentoId().equals(equipamentoId))
                .collect(Collectors.toList());
    }
}