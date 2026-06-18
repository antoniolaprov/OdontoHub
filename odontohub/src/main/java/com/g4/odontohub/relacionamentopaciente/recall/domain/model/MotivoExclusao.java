package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

/**
 * Motivos que excluem o paciente automaticamente dos processos de recall (F07).
 */
public enum MotivoExclusao {
    FALECIDO,
    TRANSFERIDO,
    ALTA_DEFINITIVA,
    BLOQUEIO_JUDICIAL;

    public static MotivoExclusao fromLabel(String label) {
        return MotivoExclusao.valueOf(label.trim().toUpperCase().replace(" ", "_").replace("-", "_"));
    }
}
