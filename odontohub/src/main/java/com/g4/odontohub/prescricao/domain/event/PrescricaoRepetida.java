package com.g4.odontohub.prescricao.domain.event;

import java.time.LocalDate;
import java.util.Objects;

import com.g4.odontohub.prescricao.domain.model.PrescricaoId;

public class PrescricaoRepetida {
    private final PrescricaoId novaPrescricaoId;
    private final Long prescricaoOrigemId;
    private final LocalDate dataNovaPrescricao;

    public PrescricaoRepetida(PrescricaoId novaPrescricaoId, Long prescricaoOrigemId,
            LocalDate dataNovaPrescricao) {
        this.novaPrescricaoId = Objects.requireNonNull(novaPrescricaoId, "ID da nova prescrição não pode ser nulo");
        this.prescricaoOrigemId = Objects.requireNonNull(prescricaoOrigemId,
                "ID da prescrição de origem não pode ser nulo");
        this.dataNovaPrescricao = Objects.requireNonNull(dataNovaPrescricao,
                "Data da nova prescrição não pode ser nula");
    }

    public PrescricaoId getNovaPrescricaoId() {
        return novaPrescricaoId;
    }

    public Long getPrescricaoOrigemId() {
        return prescricaoOrigemId;
    }

    public LocalDate getDataNovaPrescricao() {
        return dataNovaPrescricao;
    }

    @Override
    public String toString() {
        return "PrescricaoRepetida{" +
                "novaPrescricaoId=" + novaPrescricaoId +
                ", prescricaoOrigemId=" + prescricaoOrigemId +
                ", dataNovaPrescricao=" + dataNovaPrescricao +
                '}';
    }
}