package com.g4.odontohub.prescricao.domain.event;

import java.time.LocalDate;
import java.util.Objects;

import com.g4.odontohub.prescricao.domain.model.PrescricaoId;

public class PrescricaoRegistrada {
    private final PrescricaoId prescricaoId;
    private final Long pacienteId;
    private final Long dentistaId;
    private final LocalDate dataPrescricao;

    public PrescricaoRegistrada(PrescricaoId prescricaoId, Long pacienteId,
            Long dentistaId, LocalDate dataPrescricao) {
        this.prescricaoId = Objects.requireNonNull(prescricaoId, "ID da prescrição não pode ser nulo");
        this.pacienteId = Objects.requireNonNull(pacienteId, "ID do paciente não pode ser nulo");
        this.dentistaId = Objects.requireNonNull(dentistaId, "ID do dentista não pode ser nulo");
        this.dataPrescricao = Objects.requireNonNull(dataPrescricao, "Data da prescrição não pode ser nula");
    }

    public PrescricaoId getPrescricaoId() {
        return prescricaoId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getDentistaId() {
        return dentistaId;
    }

    public LocalDate getDataPrescricao() {
        return dataPrescricao;
    }

    @Override
    public String toString() {
        return "PrescricaoRegistrada{" +
                "prescricaoId=" + prescricaoId +
                ", pacienteId=" + pacienteId +
                ", dentistaId=" + dentistaId +
                ", dataPrescricao=" + dataPrescricao +
                '}';
    }
}