package com.g4.odontohub.inadimplencia.domain.model;

import java.math.BigDecimal;

public class ResumoInadimplencia {

    private final Paciente paciente;
    private final Parcela parcela;
    private final boolean semCobrancaRecente;

    public ResumoInadimplencia(Paciente paciente, Parcela parcela, boolean semCobrancaRecente) {
        this.paciente = paciente;
        this.parcela = parcela;
        this.semCobrancaRecente = semCobrancaRecente;
    }

    public Paciente getPaciente() { return paciente; }
    public Parcela getParcela() { return parcela; }

    public BigDecimal getValorOriginal() { return parcela.getValorOriginal(); }
    public long getDiasAtraso()          { return parcela.getDiasAtraso(); }
    public BigDecimal getJuros()         { return parcela.getJuros(); }
    public BigDecimal getMulta()         { return parcela.getMulta(); }
    public BigDecimal getValorAtualizado(){ return parcela.getValorAtualizado(); }

    public boolean requerDestaqueVisual() { return semCobrancaRecente; }
}
