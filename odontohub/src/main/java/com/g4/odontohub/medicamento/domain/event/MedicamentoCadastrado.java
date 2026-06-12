package com.g4.odontohub.medicamento.domain.event;

import com.g4.odontohub.medicamento.domain.model.MedicamentoId;

public record MedicamentoCadastrado(MedicamentoId medicamentoId, String nomeComercial, String classeFarmacologica) {}
