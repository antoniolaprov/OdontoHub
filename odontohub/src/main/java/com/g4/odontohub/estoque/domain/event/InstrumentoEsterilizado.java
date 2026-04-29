package com.g4.odontohub.estoque.domain.event;

import com.g4.odontohub.estoque.domain.model.InstrumentoId;
import java.time.LocalDate;

public record InstrumentoEsterilizado(
        InstrumentoId instrumentoId,
        LocalDate dataEsterilizacao,
        String responsavel,
        LocalDate novaDataVencimento
) {}