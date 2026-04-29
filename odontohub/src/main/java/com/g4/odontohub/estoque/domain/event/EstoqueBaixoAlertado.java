package com.g4.odontohub.estoque.domain.event;

public record EstoqueBaixoAlertado(Long materialId, String nomeMaterial, int saldoAtual, int pontoMinimo) {}
