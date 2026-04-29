package com.g4.odontohub.estoque.domain.event;

public record ConsumoRegistrado(Long materialId, int quantidadeConsumida, Long procedimentoOrigemId) {}
