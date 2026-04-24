package com.g4.odontohub.agendamento.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public final class HistoricoStatus {

    private final LocalDateTime dataHora;
    private final StatusAgendamento status;
    private final String responsavel;
    private final String motivo;

    public HistoricoStatus(StatusAgendamento status, String responsavel, String motivo) {
        this.dataHora = LocalDateTime.now();
        this.status = status;
        this.responsavel = responsavel;
        this.motivo = motivo;
    }
}
