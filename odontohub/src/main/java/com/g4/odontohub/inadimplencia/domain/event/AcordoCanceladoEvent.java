package com.g4.odontohub.inadimplencia.domain.event;

import java.time.LocalDateTime;

public class AcordoCanceladoEvent {
    private final String acordoId;
    private final String pacienteId;
    private final String justificativa;
    private final LocalDateTime ocorridoEm;

    public AcordoCanceladoEvent(String acordoId, String pacienteId, String justificativa) {
        this.acordoId = acordoId;
        this.pacienteId = pacienteId;
        this.justificativa = justificativa;
        this.ocorridoEm = LocalDateTime.now();
    }

    public String getAcordoId() { return acordoId; }
    public String getPacienteId() { return pacienteId; }
    public String getJustificativa() { return justificativa; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
}
