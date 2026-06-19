package com.g4.odontohub.inadimplencia.domain.event;

import java.time.LocalDateTime;

public class AcordoInadimplidoEvent {
    private final String acordoId;
    private final String pacienteId;
    private final LocalDateTime ocorridoEm;

    public AcordoInadimplidoEvent(String acordoId, String pacienteId) {
        this.acordoId = acordoId;
        this.pacienteId = pacienteId;
        this.ocorridoEm = LocalDateTime.now();
    }

    public String getAcordoId() { return acordoId; }
    public String getPacienteId() { return pacienteId; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
}
