package com.g4.odontohub.confirmacao.naocomparecimento.domain.event;

/** Emitido quando um paciente atinge o limite de faltas injustificadas (F17). */
public record PacienteReincidente(String paciente, int faltasInjustificadas) {}
