package com.g4.odontohub.relacionamentopaciente.recall.domain.model;

import java.time.LocalDateTime;

/**
 * Registro completo de uma tentativa de contato de recall (F07):
 * responsável, data/horário, canal, duração, resultado e observações.
 */
public record TentativaContato(
        String responsavel,
        LocalDateTime dataHora,
        CanalContato canal,
        int duracaoMinutos,
        ResultadoContato resultado,
        String observacoes) {
}
