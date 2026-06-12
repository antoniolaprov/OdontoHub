package com.g4.odontohub.pagamento.domain.event;

import com.g4.odontohub.pagamento.domain.model.PagamentoId;

public record PagamentoCancelado(PagamentoId pagamentoId, String justificativa) {}
