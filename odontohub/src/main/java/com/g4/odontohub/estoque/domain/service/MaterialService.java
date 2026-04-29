package com.g4.odontohub.estoque.domain.service;

import com.g4.odontohub.estoque.domain.model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialService {

    private final Map<String, MaterialConsumivel> materiais = new HashMap<>();
    private long nextMaterialId = 1L;
    private long nextReposicaoId = 1L;

    public MaterialConsumivel cadastrarMaterial(String nome, String unidadeMedida,
                                                int saldoInicial, int pontoMinimo) {
        MaterialConsumivel m = new MaterialConsumivel(
                new MaterialId(nextMaterialId++), nome, unidadeMedida,
                saldoInicial, pontoMinimo, LocalDate.now());
        materiais.put(nome, m);
        return m;
    }

    public MaterialConsumivel buscarPorNome(String nome) {
        return materiais.get(nome);
    }

    public List<Object> registrarReposicao(String materialNome, String fornecedor,
                                           int quantidade, double custoUnitario) {
        if (quantidade <= 0 || custoUnitario <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade e custo unitário devem ser valores positivos");
        }
        MaterialConsumivel material = materiais.get(materialNome);
        Reposicao r = new Reposicao(
                new ReposicaoId(nextReposicaoId++),
                new ReposicaomaterialId(material.getId().id()),
                fornecedor, quantidade, custoUnitario, LocalDate.now());
        return material.registrarReposicao(r);
    }

    public List<Object> descontarConsumo(String materialNome, int quantidade, Long procedimentoId) {
        MaterialConsumivel material = materiais.get(materialNome);
        return material.registrarConsumo(quantidade, procedimentoId);
    }

    public void ajustarSaldo(String materialNome, int novaQuantidade) {
        materiais.get(materialNome).ajustarSaldo(novaQuantidade);
    }
}
