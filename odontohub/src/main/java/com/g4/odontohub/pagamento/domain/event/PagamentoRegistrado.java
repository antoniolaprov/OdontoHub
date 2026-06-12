package com.g4.odontohub.pagamento.domain.event;

import com.g4.odontohub.pagamento.domain.model.PagamentoId;

public record PagamentoRegistrado(PagamentoId pagamentoId, String parcelaReferencia, double valorRecebido) {}
