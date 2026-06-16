package com.g4.odontohub.confirmacao.lembrete.domain.model;

/** Canal pelo qual o lembrete de consulta é enviado (F13). */
public enum CanalLembrete {
    SMS,
    WHATSAPP,
    EMAIL;

    public static CanalLembrete fromLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("O canal do lembrete é obrigatório");
        }
        return switch (label.trim().toUpperCase()) {
            case "SMS" -> SMS;
            case "WHATSAPP", "WHATS", "ZAP" -> WHATSAPP;
            case "EMAIL", "E-MAIL" -> EMAIL;
            default -> throw new IllegalArgumentException("Canal inválido: " + label);
        };
    }
}
