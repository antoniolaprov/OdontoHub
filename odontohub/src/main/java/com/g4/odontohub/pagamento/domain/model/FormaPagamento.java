package com.g4.odontohub.pagamento.domain.model;

public enum FormaPagamento {
    PIX,
    CARTAO_CREDITO,
    CARTAO_DEBITO,
    DINHEIRO,
    TRANSFERENCIA;

    public static FormaPagamento fromLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("A forma de pagamento é obrigatória");
        }
        return FormaPagamento.valueOf(label.trim().toUpperCase());
    }
}
