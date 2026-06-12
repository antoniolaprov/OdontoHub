package com.g4.odontohub.prescricao.domain.event;

import com.g4.odontohub.prescricao.domain.model.PrescricaoId;

import java.time.LocalDate;

public record PrescricaoRegistrada(PrescricaoId prescricaoId, String paciente, String dentista,
                                   LocalDate dataPrescricao) {}
