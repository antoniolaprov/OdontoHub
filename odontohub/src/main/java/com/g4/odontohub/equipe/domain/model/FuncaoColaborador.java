package com.g4.odontohub.equipe.domain.model;

/**
 * Função do colaborador (F12). Cada função carrega um conjunto de permissões
 * definidas automaticamente conforme o documento de domínio:
 * - apenas Especialistas podem validar procedimentos;
 * - apenas Administradores podem alterar permissões;
 * - Auxiliares não podem acessar dados financeiros.
 */
public enum FuncaoColaborador {
    AUXILIAR,
    RECEPCIONISTA,
    ESPECIALISTA,
    ADMINISTRADOR;

    public boolean podeValidarProcedimentos() {
        return this == ESPECIALISTA;
    }

    public boolean podeAlterarPermissoes() {
        return this == ADMINISTRADOR;
    }

    public boolean podeAcessarFinanceiro() {
        return this != AUXILIAR;
    }

    public static FuncaoColaborador fromLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("A função do colaborador é obrigatória");
        }
        return switch (label.trim().toUpperCase()) {
            case "AUXILIAR" -> AUXILIAR;
            case "RECEPCIONISTA" -> RECEPCIONISTA;
            case "ESPECIALISTA" -> ESPECIALISTA;
            case "ADMINISTRADOR" -> ADMINISTRADOR;
            default -> throw new IllegalArgumentException("Função inválida: " + label);
        };
    }
}
