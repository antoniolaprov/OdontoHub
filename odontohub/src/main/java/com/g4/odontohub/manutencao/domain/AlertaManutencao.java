package com.g4.odontohub.manutencao.domain;

import lombok.Getter;

@Getter
public class AlertaManutencao {

    private final String nomeEquipamento;
    private final long diasRestantes;

    private AlertaManutencao(String nomeEquipamento, long diasRestantes) {
        this.nomeEquipamento = nomeEquipamento;
        this.diasRestantes = diasRestantes;
    }

    public static AlertaManutencao criar(String nomeEquipamento, long diasRestantes) {
        return new AlertaManutencao(nomeEquipamento, diasRestantes);
    }
}