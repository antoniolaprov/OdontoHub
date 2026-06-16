package com.g4.odontohub.confirmacao.lembrete.domain.event;

import com.g4.odontohub.confirmacao.lembrete.domain.model.LembreteId;

/**
 * Emitido quando o paciente confirma a consulta a partir de um lembrete (F13).
 * Permite que o contexto de Agendamento (F1) marque o agendamento como confirmado.
 */
public record ConsultaConfirmadaPeloPaciente(LembreteId lembreteId, Long agendamentoId, String paciente) {}
