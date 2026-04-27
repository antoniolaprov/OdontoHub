package com.g4.odontohub.manutencao.domain;

public interface EquipamentoRepository {
    void salvar(Equipamento equipamento);
    Equipamento buscarPorNome(String nome);
}