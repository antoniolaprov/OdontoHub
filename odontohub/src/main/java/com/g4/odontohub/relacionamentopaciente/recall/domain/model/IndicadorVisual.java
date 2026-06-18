package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

/**
 * Indicador visual automático do paciente na fila de recall (F07).
 * Verde → retorno preventivo comum; Amarelo → atraso moderado;
 * Vermelho → tratamento crítico interrompido; Roxo → inadimplência associada;
 * Azul → pós-operatório prioritário.
 */
public enum IndicadorVisual {
    VERDE,
    AMARELO,
    VERMELHO,
    ROXO,
    AZUL;
}
