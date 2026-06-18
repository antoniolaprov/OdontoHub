package com.g4.odontohub.equipe.domain.model;

import java.time.LocalDateTime;

/** Registro de auditoria de uma ação executada por um colaborador (F12). */
public record RegistroAuditoria(LocalDateTime dataHora, ModuloSistema modulo, String acao) {
}
