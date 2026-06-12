package com.g4.odontohub.relacionamentopaciente.followup.domain.model;

import java.time.LocalDate;

public record ChecklistFollowup(boolean sangramentoAtivo, int nivelDor, String observacoes,
                                LocalDate dataLigacao, String responsavel) {

    public boolean indicaEmergencia() {
        return sangramentoAtivo || nivelDor > 7;
    }
}
