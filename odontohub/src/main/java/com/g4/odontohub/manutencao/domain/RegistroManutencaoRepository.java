package com.g4.odontohub.manutencao.domain;

import java.util.List;
import java.util.UUID;

public interface RegistroManutencaoRepository {
    void salvar(RegistroManutencao registro);
    List<RegistroManutencao> buscarPorEquipamento(UUID equipamentoId);
}