package com.g4.odontohub.relacionamentopaciente.domain.model;

import java.time.LocalDateTime;

public record ChecklistFollowup(
        boolean sangramentoAtivo,
        int nivelDor,
        String observacoes,
        LocalDateTime dataLigacao,
        String responsavel) {}
