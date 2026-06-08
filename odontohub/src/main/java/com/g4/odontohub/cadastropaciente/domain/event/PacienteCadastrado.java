package com.g4.odontohub.cadastropaciente.domain.event;

import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;
import com.g4.odontohub.cadastropaciente.domain.model.StatusPaciente;

public record PacienteCadastrado(
        PacienteRegistroId pacienteId,
        String nomeCompleto,
        String cpf,
        StatusPaciente status
) {}
