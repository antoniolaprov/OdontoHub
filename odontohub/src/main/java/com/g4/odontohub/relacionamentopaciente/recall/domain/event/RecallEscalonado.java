package com.g4.odontohub.relacionamentopaciente.recall.domain.event;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.RecallId;

/**
 * Emitido quando um recall atinge o limite de tentativas de contato sem conversão
 * e precisa ser escalonado para um responsável (F07 — SLA de relacionamento).
 */
public record RecallEscalonado(RecallId recallId, String paciente, int tentativas) {}
