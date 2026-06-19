package com.g4.odontohub.clinica.domain.event;

import com.g4.odontohub.clinica.domain.model.ClinicaId;

public record ClinicaCadastrada(
        ClinicaId clinicaId,
        String nome,
        String cnpj,
        String email
) {}
