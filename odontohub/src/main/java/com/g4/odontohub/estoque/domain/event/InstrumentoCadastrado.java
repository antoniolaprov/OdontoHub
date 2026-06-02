package com.g4.odontohub.estoque.domain.event;

import com.g4.odontohub.estoque.domain.model.InstrumentoId;

public record InstrumentoCadastrado(
        InstrumentoId instrumentoId,
        String nome,
        String categoria,
        String codigoIdentificador
) {}
