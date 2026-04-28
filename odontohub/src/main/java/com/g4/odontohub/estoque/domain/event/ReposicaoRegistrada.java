package com.g4.odontohub.estoque.domain.event;

public record ReposicaoRegistrada(
        Long reposicaoId,
        Long materialId,
        int quantidade,
        double custoUnitario,
        double valorTotalLancamento
) {}
