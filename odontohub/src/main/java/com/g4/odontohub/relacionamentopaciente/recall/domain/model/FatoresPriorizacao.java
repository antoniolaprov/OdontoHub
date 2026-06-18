package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

/**
 * Fatores de entrada do motor de priorização inteligente do recall (F07).
 * A partir deles o agregado {@link Recall} calcula a pontuação de prioridade
 * e classifica automaticamente a categoria do recall.
 */
public record FatoresPriorizacao(
        int mesesSemRetorno,
        int tratamentosPendentes,
        boolean procedimentoCritico,
        boolean inadimplente,
        int historicoFaltas,
        int cancelamentosRecorrentes,
        boolean riscoClinicoAlto,
        double potencialFinanceiro,
        boolean tratamentoInterrompido,
        boolean pacienteEvadido) {

    public static FatoresPriorizacao vazio() {
        return new FatoresPriorizacao(0, 0, false, false, 0, 0, false, 0.0, false, false);
    }
}
