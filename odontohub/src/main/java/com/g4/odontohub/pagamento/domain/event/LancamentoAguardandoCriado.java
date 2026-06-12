package com.g4.odontohub.pagamento.domain.event;

import com.g4.odontohub.pagamento.domain.model.PagamentoId;

public record LancamentoAguardandoCriado(PagamentoId pagamentoId, String parcelaReferencia) {}
