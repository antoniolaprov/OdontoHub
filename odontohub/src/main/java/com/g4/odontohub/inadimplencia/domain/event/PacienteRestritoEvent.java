package com.g4.odontohub.inadimplencia.domain.event;

import java.time.LocalDateTime;

public class PacienteRestritoEvent {
    private final String pacienteId;
    private final LocalDateTime ocorridoEm;

    public PacienteRestritoEvent(String pacienteId) {
        this.pacienteId = pacienteId;
        this.ocorridoEm = LocalDateTime.now();
    }

    public String getPacienteId() { return pacienteId; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
}
