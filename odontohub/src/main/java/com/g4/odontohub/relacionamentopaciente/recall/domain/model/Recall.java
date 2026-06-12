package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

public class Recall {

    private final RecallId id;
    private final String paciente;
    private final String procedimentoGatilho;
    private final int diasParaRetorno;
    private StatusRecall status;
    private boolean flagConversaoRecall;

    public Recall(RecallId id, String paciente, String procedimentoGatilho, int diasParaRetorno) {
        this.id = id;
        this.paciente = paciente;
        this.procedimentoGatilho = procedimentoGatilho;
        this.diasParaRetorno = diasParaRetorno;
        this.status = StatusRecall.NA_FILA;
    }

    public void cancelar() {
        this.status = StatusRecall.CANCELADO;
    }

    public void converter() {
        this.status = StatusRecall.CONVERTIDO;
        this.flagConversaoRecall = true;
    }

    public RecallId getId() { return id; }
    public String getPaciente() { return paciente; }
    public String getProcedimentoGatilho() { return procedimentoGatilho; }
    public int getDiasParaRetorno() { return diasParaRetorno; }
    public StatusRecall getStatus() { return status; }
    public boolean isFlagConversaoRecall() { return flagConversaoRecall; }
}
