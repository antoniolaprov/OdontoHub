package com.g4.odontohub.inadimplencia.domain.event;

import java.time.LocalDateTime;

public class RestricaoRemovidaEvent {
    private final String pacienteId;
    private final LocalDateTime ocorridoEm;

    public RestricaoRemovidaEvent(String pacienteId) {
        this.pacienteId = pacienteId;
        this.ocorridoEm = LocalDateTime.now();
    }

    public String getPacienteId() { return pacienteId; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
}
