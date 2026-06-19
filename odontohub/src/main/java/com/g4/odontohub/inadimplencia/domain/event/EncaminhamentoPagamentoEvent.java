package com.g4.odontohub.inadimplencia.domain.event;

import java.time.LocalDateTime;

public class EncaminhamentoPagamentoEvent {
    private final String parcelaId;
    private final String pacienteId;
    private final LocalDateTime ocorridoEm;

    public EncaminhamentoPagamentoEvent(String parcelaId, String pacienteId) {
        this.parcelaId = parcelaId;
        this.pacienteId = pacienteId;
        this.ocorridoEm = LocalDateTime.now();
    }

    public String getParcelaId() { return parcelaId; }
    public String getPacienteId() { return pacienteId; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
}
