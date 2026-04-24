package com.g4.odontohub.agendamento.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AgendamentoRepository {
    Agendamento salvar(Agendamento agendamento);
    Optional<Agendamento> buscarPorId(Long id);
    boolean existeConflito(LocalDateTime dataHora);
}
