package com.g4.odontohub.cadastropaciente.domain.event;

import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;

public record PacienteCadastradoRapido(
        PacienteRegistroId pacienteId,
        String nomeCompleto,
        String telefone
) {}
