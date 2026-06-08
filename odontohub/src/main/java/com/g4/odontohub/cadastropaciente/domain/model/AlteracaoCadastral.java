package com.g4.odontohub.cadastropaciente.domain.model;

import java.time.LocalDate;

public record AlteracaoCadastral(
        String campo,
        String valorAnterior,
        String valorAtualizado,
        String responsavel,
        LocalDate dataAlteracao
) {}
