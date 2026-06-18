package com.g4.odontohub.equipe.domain.model;

import java.time.LocalDateTime;

/** Histórico de uma alteração cadastral de colaborador (F12). */
public record AlteracaoCadastral(String campo, String valorAnterior, String valorAtualizado,
                                 String responsavel, LocalDateTime data) {
}
