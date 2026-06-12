package com.g4.odontohub.medicamento.domain.event;

import com.g4.odontohub.medicamento.domain.model.MedicamentoId;

public record MedicamentoInativado(MedicamentoId medicamentoId, String justificativa) {}
