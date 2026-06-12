package com.g4.odontohub.estoque.domain.service;

import com.g4.odontohub.estoque.domain.model.*;
import com.g4.odontohub.estoque.domain.repository.MaterialRepository;

import java.time.LocalDate;
import java.util.List;

public class MaterialService {

    private final MaterialRepository repositorio;
    private long nextReposicaoId = 1L;

    public MaterialService(MaterialRepository repositorio) {
        this.repositorio = repositorio;
    }

    public MaterialConsumivel cadastrarMaterial(String nome, String unidadeMedida,
                                                int saldoInicial, int pontoMinimo) {
        MaterialConsumivel m = new MaterialConsumivel(
                new MaterialId(repositorio.proximoId()), nome, unidadeMedida,
                saldoInicial, pontoMinimo, LocalDate.now());
        repositorio.salvar(m);
        return m;
    }

    public MaterialConsumivel buscarPorNome(String nome) {
        return repositorio.buscarPorNome(nome);
    }

    public List<Object> registrarReposicao(String materialNome, String fornecedor,
                                           int quantidade, double custoUnitario) {
        if (quantidade <= 0 || custoUnitario <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade e custo unitário devem ser valores positivos");
        }
        MaterialConsumivel material = repositorio.buscarPorNome(materialNome);
        Reposicao r = new Reposicao(
                new ReposicaoId(nextReposicaoId++),
                new ReposicaomaterialId(material.getId().id()),
                fornecedor, quantidade, custoUnitario, LocalDate.now());
        List<Object> eventos = material.registrarReposicao(r);
        repositorio.salvar(material);
        return eventos;
    }

    public List<Object> descontarConsumo(String materialNome, int quantidade, Long procedimentoId) {
        MaterialConsumivel material = repositorio.buscarPorNome(materialNome);
        List<Object> eventos = material.registrarConsumo(quantidade, procedimentoId);
        repositorio.salvar(material);
        return eventos;
    }

    public void ajustarSaldo(String materialNome, int novaQuantidade) {
        MaterialConsumivel material = repositorio.buscarPorNome(materialNome);
        material.ajustarSaldo(novaQuantidade);
        repositorio.salvar(material);
    }
}
