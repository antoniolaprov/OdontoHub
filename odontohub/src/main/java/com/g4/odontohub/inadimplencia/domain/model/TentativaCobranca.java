package com.g4.odontohub.inadimplencia.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class TentativaCobranca {

    private final String id;
    private final String pacienteId;
    private final LocalDate dataContato;
    private final String responsavel;
    private final CanalCobranca canal;
    private final String resultado;
    private final String observacao;

    public TentativaCobranca(String pacienteId, String responsavel,
                              CanalCobranca canal, String resultado, String observacao) {
        this.id = UUID.randomUUID().toString();
        this.pacienteId = pacienteId;
        this.dataContato = LocalDate.now();
        this.responsavel = responsavel;
        this.canal = canal;
        this.resultado = resultado;
        this.observacao = observacao;
    }

    public boolean isRecente(int dias) {
        return !dataContato.isBefore(LocalDate.now().minusDays(dias));
    }

    public String getId() { return id; }
    public String getPacienteId() { return pacienteId; }
    public LocalDate getDataContato() { return dataContato; }
    public String getResponsavel() { return responsavel; }
    public CanalCobranca getCanal() { return canal; }
    public String getResultado() { return resultado; }
    public String getObservacao() { return observacao; }
}
