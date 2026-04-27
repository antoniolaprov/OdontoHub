package com.g4.odontohub.orcamento.domain;

import com.g4.odontohub.shared.exception.DomainException;

import lombok.Getter;

@Getter
public class ItemOrcamento {
    private String descricao;
    private float valorUnitario;

    private ItemOrcamento() {}

    public static ItemOrcamento criar(String descricao, float valorUnitario) {
        ItemOrcamento item = new ItemOrcamento();
        item.descricao = descricao;
        item.valorUnitario = valorUnitario;
        return item;
    }
    public void alterarValor(float novoValor) {
    if (novoValor < 0) {
        throw new DomainException("Valor não pode ser negativo");
    }
    this.valorUnitario = novoValor;
}
}