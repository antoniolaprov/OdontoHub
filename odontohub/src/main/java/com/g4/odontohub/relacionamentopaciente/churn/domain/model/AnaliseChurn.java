package com.g4.odontohub.relacionamentopaciente.churn.domain.model;

public class AnaliseChurn {

    private final ChurnId id;
    private final String paciente;
    private boolean possuiAgendamentoFuturo;
    private int mesesSemRetorno;
    private boolean possuiPlanoAtivo;
    private StatusChurn statusChurn;

    public AnaliseChurn(ChurnId id, String paciente) {
        this.id = id;
        this.paciente = paciente;
        this.statusChurn = StatusChurn.ATIVO;
    }

    public void classificar(StatusChurn status) {
        this.statusChurn = status;
    }

    public ChurnId getId() { return id; }
    public String getPaciente() { return paciente; }
    public boolean isPossuiAgendamentoFuturo() { return possuiAgendamentoFuturo; }
    public int getMesesSemRetorno() { return mesesSemRetorno; }
    public boolean isPossuiPlanoAtivo() { return possuiPlanoAtivo; }
    public StatusChurn getStatusChurn() { return statusChurn; }

    public void setPossuiAgendamentoFuturo(boolean possuiAgendamentoFuturo) {
        this.possuiAgendamentoFuturo = possuiAgendamentoFuturo;
    }

    public void setMesesSemRetorno(int mesesSemRetorno) {
        this.mesesSemRetorno = mesesSemRetorno;
    }

    public void setPossuiPlanoAtivo(boolean possuiPlanoAtivo) {
        this.possuiPlanoAtivo = possuiPlanoAtivo;
    }
}
