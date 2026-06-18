package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

/** Canal utilizado em uma tentativa de contato de recall (F07). */
public enum CanalContato {
    TELEFONE,
    SMS,
    WHATSAPP,
    EMAIL;

    public static CanalContato fromLabel(String label) {
        return CanalContato.valueOf(label.trim().toUpperCase());
    }
}
