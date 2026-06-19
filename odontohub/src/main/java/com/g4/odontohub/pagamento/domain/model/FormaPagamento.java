package com.g4.odontohub.pagamento.domain.model;

import java.text.Normalizer;

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
        String normalizado = Normalizer.normalize(label.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace(' ', '_')
                .toUpperCase()
                .replace("_DE_", "_");
        return FormaPagamento.valueOf(normalizado);
    }
}
