package com.g4.odontohub.cadastropaciente.domain.event;

import com.g4.odontohub.cadastropaciente.domain.model.AlteracaoCadastral;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;

public record CadastroPacienteAtualizado(
        PacienteRegistroId pacienteId,
        AlteracaoCadastral alteracao
) {}
