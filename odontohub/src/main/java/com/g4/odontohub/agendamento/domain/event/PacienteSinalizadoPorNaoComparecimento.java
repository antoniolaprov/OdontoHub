package com.g4.odontohub.agendamento.domain.event;

public record PacienteSinalizadoPorNaoComparecimento(
        Long pacienteId,
        int quantidadeNoShowsRecentes,
        String classificacaoRisco) {}
