package com.g4.odontohub.estoque.domain.model;

import java.time.LocalDate;

public class Reposicao {

    private final ReposicaoId id;
    private final ReposicaomaterialId materialId;
    private final String fornecedor;
    private final int quantidade;
    private final double custoUnitario;
    private final LocalDate dataEntrada;

    public Reposicao(ReposicaoId id, ReposicaomaterialId materialId, String fornecedor,
                     int quantidade, double custoUnitario, LocalDate dataEntrada) {
        this.id = id;
        this.materialId = materialId;
        this.fornecedor = fornecedor;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.dataEntrada = dataEntrada;
    }

    public ReposicaoId getId() { return id; }
    public ReposicaomaterialId getMaterialId() { return materialId; }
    public String getFornecedor() { return fornecedor; }
    public int getQuantidade() { return quantidade; }
    public double getCustoUnitario() { return custoUnitario; }
    public LocalDate getDataEntrada() { return dataEntrada; }
}
