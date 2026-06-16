package com.g4.odontohub.equipe.domain.model;

/**
 * Status operacional do colaborador (F12). Colaboradores Inativos ou Suspensos
 * não podem realizar login no sistema.
 */
public enum StatusColaborador {
    ATIVO,
    INATIVO,
    SUSPENSO,
    FERIAS,
    AFASTADO;

    /** Inativos e suspensos não podem autenticar (regra de segurança da F12). */
    public boolean permiteLogin() {
        return this != INATIVO && this != SUSPENSO;
    }

    public static StatusColaborador fromLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("O status do colaborador é obrigatório");
        }
        return switch (label.trim().toUpperCase()) {
            case "ATIVO" -> ATIVO;
            case "INATIVO" -> INATIVO;
            case "SUSPENSO" -> SUSPENSO;
            case "FERIAS", "FÉRIAS" -> FERIAS;
            case "AFASTADO" -> AFASTADO;
            default -> throw new IllegalArgumentException("Status inválido: " + label);
        };
    }
}
