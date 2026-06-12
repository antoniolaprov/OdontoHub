package com.g4.odontohub.prescricao.domain.event;

import com.g4.odontohub.prescricao.domain.model.PrescricaoId;

import java.time.LocalDate;

public record PrescricaoRepetida(PrescricaoId novaPrescricaoId, Long prescricaoOrigemId, LocalDate dataNovaPrescricao) {}
