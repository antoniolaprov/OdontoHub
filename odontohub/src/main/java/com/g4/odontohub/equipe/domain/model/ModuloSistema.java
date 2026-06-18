package com.g4.odontohub.equipe.domain.model;

/** Módulo do sistema sobre o qual uma ação auditável foi executada (F12). */
public enum ModuloSistema {
    PROCEDIMENTO,
    ESTERILIZACAO,
    AGENDAMENTO,
    PAGAMENTO,
    CADASTRO,
    EQUIPE;

    public static ModuloSistema fromLabel(String label) {
        return ModuloSistema.valueOf(label.trim().toUpperCase());
    }
}
