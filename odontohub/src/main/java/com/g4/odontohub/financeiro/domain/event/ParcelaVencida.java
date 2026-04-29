package com.g4.odontohub.financeiro.domain.event;

import com.g4.odontohub.financeiro.domain.model.ParcelaId;

public record ParcelaVencida(ParcelaId parcelaId, int diasAtraso, double multa, double juros) {}
