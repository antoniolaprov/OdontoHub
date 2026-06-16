package com.g4.odontohub.confirmacao.lembrete.domain.model;

/** Ciclo de vida do lembrete de consulta (F13). */
public enum StatusLembrete {
    PENDENTE,
    ENVIADO,
    CONFIRMADO,
    RECUSADO,
    SEM_RESPOSTA;

    public static StatusLembrete fromLabel(String label) {
        return StatusLembrete.valueOf(label.trim().toUpperCase().replace(" ", "_").replace("-", "_"));
    }
}
