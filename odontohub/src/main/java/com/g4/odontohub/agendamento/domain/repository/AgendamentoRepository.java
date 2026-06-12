package com.g4.odontohub.agendamento.domain.repository;

import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.AgendamentoId;

import java.util.List;

/** Porta de persistência do contexto de Agendamento (camada de domínio). */
public interface AgendamentoRepository {

    void salvar(Agendamento agendamento);

    Agendamento buscarPorId(AgendamentoId id);

    List<Agendamento> todos();

    long proximoId();
}
