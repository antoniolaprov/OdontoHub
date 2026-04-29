package com.g4.odontohub.financeiro.domain.event;

import com.g4.odontohub.financeiro.domain.model.LancamentoId;

public record SaidaRegistradaManualmente(LancamentoId lancamentoId, double valor, String categoria) {}
