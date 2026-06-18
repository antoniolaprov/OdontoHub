package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

/**
 * Segmentação automática da fila de recall (F07).
 * Cada categoria pode possuir regras próprias de contato e prioridade.
 */
public enum CategoriaRecall {
    PREVENTIVO,
    POS_OPERATORIO,
    TRATAMENTO_INTERROMPIDO,
    FINANCEIRO,
    REATIVACAO;

    public static CategoriaRecall fromLabel(String label) {
        return CategoriaRecall.valueOf(label.trim().toUpperCase().replace(" ", "_").replace("-", "_"));
    }
}
