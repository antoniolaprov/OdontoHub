package com.g4.odontohub.financeiro.domain.event;

import com.g4.odontohub.financeiro.domain.model.AcordoId;

public record AcordoFirmado(AcordoId acordoId, String parcelasOriginaisSubstituidas, int quantidadeNovasParcelas) {}
