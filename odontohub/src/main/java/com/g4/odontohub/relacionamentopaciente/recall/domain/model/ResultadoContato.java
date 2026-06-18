package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

/**
 * Resultado possível de uma tentativa de contato de recall (F07).
 * Resultados "sem sucesso" alimentam o escalonamento por SLA.
 */
public enum ResultadoContato {
    CONTATO_REALIZADO(false),
    SEM_RESPOSTA(true),
    NUMERO_INVALIDO(true),
    RETORNO_AGENDADO(false),
    PACIENTE_RECUSOU(false),
    SOLICITAR_NOVO_CONTATO(true);

    private final boolean semSucesso;

    ResultadoContato(boolean semSucesso) {
        this.semSucesso = semSucesso;
    }

    public boolean semSucesso() {
        return semSucesso;
    }

    public static ResultadoContato fromLabel(String label) {
        return ResultadoContato.valueOf(label.trim().toUpperCase().replace(" ", "_"));
    }
}
