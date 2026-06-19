package com.g4.odontohub.financeiro.domain.model;

import java.time.LocalDate;

public class ContatoCobranca {

    private final LocalDate dataContato;
    private final String responsavel;
    private final String canal;
    private final String resultado;
    private final String observacao;

    public ContatoCobranca(LocalDate dataContato, String responsavel, String canal,
                           String resultado, String observacao) {
        this.dataContato = dataContato;
        this.responsavel = responsavel;
        this.canal = canal;
        this.resultado = resultado;
        this.observacao = observacao;
    }

    public LocalDate getDataContato() { return dataContato; }
    public String getResponsavel() { return responsavel; }
    public String getCanal() { return canal; }
    public String getResultado() { return resultado; }
    public String getObservacao() { return observacao; }
}
