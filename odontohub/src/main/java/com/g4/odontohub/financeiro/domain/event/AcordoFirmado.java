package com.g4.odontohub.financeiro.domain.event;

public record AcordoFirmado(
        Long acordoId,
        int quantidadeParcelasOriginais,
        int quantidadeNovasParcelas) {
}
