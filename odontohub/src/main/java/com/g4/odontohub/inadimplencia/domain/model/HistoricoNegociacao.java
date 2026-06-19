package com.g4.odontohub.inadimplencia.domain.model;

import java.time.LocalDateTime;

public class HistoricoNegociacao {

    private final String responsavel;
    private final StatusAcordo statusAnterior;
    private final StatusAcordo novoStatus;
    private final String justificativa;
    private final LocalDateTime dataAlteracao;

    public HistoricoNegociacao(String responsavel, StatusAcordo statusAnterior,
                                StatusAcordo novoStatus, String justificativa) {
        this.responsavel = responsavel;
        this.statusAnterior = statusAnterior;
        this.novoStatus = novoStatus;
        this.justificativa = justificativa;
        this.dataAlteracao = LocalDateTime.now();
    }

    public String getResponsavel() { return responsavel; }
    public StatusAcordo getStatusAnterior() { return statusAnterior; }
    public StatusAcordo getNovoStatus() { return novoStatus; }
    public String getJustificativa() { return justificativa; }
    public LocalDateTime getDataAlteracao() { return dataAlteracao; }
}
