package com.g4.odontohub.orcamento.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Orcamento {
    private Long id;
    private Long planoTratamentoId;
    private Long pacienteId;
    private StatusOrcamento status;
    private float valorTotal;
    private List<ItemOrcamento> itens;
    private boolean complementar;
    private LocalDate dataAprovacao;
    private String formaAprovacao;

    private Orcamento() {}

    public static Orcamento criar(Long id, Long planoTratamentoId, Long pacienteId, List<ItemOrcamento> itens) {
        Orcamento orcamento = new Orcamento();
        orcamento.id = id;
        orcamento.planoTratamentoId = planoTratamentoId;
        orcamento.pacienteId = pacienteId;
        orcamento.status = StatusOrcamento.PENDENTE;
        orcamento.itens = new ArrayList<>(itens);
        orcamento.complementar = false;
        orcamento.valorTotal = calcularTotal(itens);
        return orcamento;
    }
    public void alterarItem(String descricaoItem, float novoValor) {
    validarAlteracaoItem(descricaoItem);

    ItemOrcamento item = itens.stream()
            .filter(i -> i.getDescricao().equalsIgnoreCase(descricaoItem))
            .findFirst()
            .orElseThrow(() -> new DomainException("Item não encontrado: " + descricaoItem));

    item.alterarValor(novoValor);

    this.valorTotal = calcularTotal(this.itens);
}

    public static Orcamento criarComplementar(Long id, Long planoTratamentoId, Long pacienteId, List<ItemOrcamento> itens) {
        Orcamento orcamento = criar(id, planoTratamentoId, pacienteId, itens);
        orcamento.complementar = true;
        return orcamento;
    }

    private static float calcularTotal(List<ItemOrcamento> itens) {
        return itens.stream()
                .map(ItemOrcamento::getValorUnitario)
                .reduce(0.0f, Float::sum);
    }

    public void aprovar(LocalDate data, String forma) {
        if (this.status == StatusOrcamento.APROVADO) {
            throw new DomainException("Orçamentos aprovados não podem ser alterados retroativamente");
        }
        this.status = StatusOrcamento.APROVADO;
        this.dataAprovacao = data;
        this.formaAprovacao = forma;
    }

    public void validarAlteracaoItem(String descricaoItem) {
        if (this.status == StatusOrcamento.APROVADO) {
            throw new DomainException("Orçamentos aprovados não podem ser alterados retroativamente");
        }
    }

    public List<ItemOrcamento> getItens() {
        return Collections.unmodifiableList(itens);
    }
}