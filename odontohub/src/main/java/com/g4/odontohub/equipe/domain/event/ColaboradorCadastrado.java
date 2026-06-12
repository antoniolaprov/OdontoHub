package com.g4.odontohub.equipe.domain.event;

import com.g4.odontohub.equipe.domain.model.ColaboradorId;
import com.g4.odontohub.equipe.domain.model.FuncaoColaborador;

public record ColaboradorCadastrado(ColaboradorId colaboradorId, FuncaoColaborador funcao) {}
