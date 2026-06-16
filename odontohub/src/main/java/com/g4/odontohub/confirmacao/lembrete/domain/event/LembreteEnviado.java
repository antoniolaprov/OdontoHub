package com.g4.odontohub.confirmacao.lembrete.domain.event;

import com.g4.odontohub.confirmacao.lembrete.domain.model.LembreteId;

public record LembreteEnviado(LembreteId lembreteId, Long agendamentoId, String canal) {}
