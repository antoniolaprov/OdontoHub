package com.g4.odontohub.equipe.domain.model;

public enum FuncaoColaborador {
    AUXILIAR,
    RECEPCIONISTA;

    public static FuncaoColaborador fromLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("A função do colaborador é obrigatória");
        }
        return switch (label.trim().toUpperCase()) {
            case "AUXILIAR" -> AUXILIAR;
            case "RECEPCIONISTA" -> RECEPCIONISTA;
            default -> throw new IllegalArgumentException("Função inválida: " + label);
        };
    }
}
