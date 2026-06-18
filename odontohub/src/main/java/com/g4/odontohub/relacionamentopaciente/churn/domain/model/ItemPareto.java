package com.g4.odontohub.relacionamentopaciente.churn.domain.model;

/**
 * Item do gráfico de Pareto dos motivos de cancelamento (F11).
 * Apresenta a quantidade, o percentual individual e o percentual acumulado,
 * permitindo identificar os poucos motivos que concentram a maior parte dos cancelamentos.
 */
public record ItemPareto(String motivo, int quantidade, double percentual, double percentualAcumulado) {
}
