package com.g4.odontohub.confirmacao.naocomparecimento.domain.event;

import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimentoId;

/**
 * Emitido ao registrar um não comparecimento (F14). Faltas injustificadas
 * alimentam a análise de churn/absenteísmo (F11).
 */
public record FaltaRegistrada(NaoComparecimentoId id, Long agendamentoId, String paciente, boolean justificada) {}
