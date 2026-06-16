package com.g4.odontohub.confirmacao.lembrete.domain.repository;

import com.g4.odontohub.confirmacao.lembrete.domain.model.Lembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.model.LembreteId;

import java.util.List;

/** Porta de persistência do contexto de Lembrete (camada de domínio). */
public interface LembreteRepository {

    void salvar(Lembrete lembrete);

    Lembrete buscarPorId(LembreteId id);

    Lembrete buscarPorAgendamento(Long agendamentoId);

    List<Lembrete> todos();

    long proximoId();
}
