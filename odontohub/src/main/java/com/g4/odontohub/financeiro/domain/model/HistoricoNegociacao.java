package com.g4.odontohub.financeiro.domain.model;

import java.time.LocalDate;

public class HistoricoNegociacao {

    private final String responsavel;
    private final LocalDate dataAlteracao;
    private final StatusAcordo statusAnterior;
    private final StatusAcordo novoStatus;
    private final String justificativa;

    public HistoricoNegociacao(String responsavel, LocalDate dataAlteracao,
                               StatusAcordo statusAnterior, StatusAcordo novoStatus,
                               String justificativa) {
        this.responsavel = responsavel;
        this.dataAlteracao = dataAlteracao;
        this.statusAnterior = statusAnterior;
        this.novoStatus = novoStatus;
        this.justificativa = justificativa;
    }

    public String getResponsavel() { return responsavel; }
    public LocalDate getDataAlteracao() { return dataAlteracao; }
    public StatusAcordo getStatusAnterior() { return statusAnterior; }
    public StatusAcordo getNovoStatus() { return novoStatus; }
    public String getJustificativa() { return justificativa; }
}
