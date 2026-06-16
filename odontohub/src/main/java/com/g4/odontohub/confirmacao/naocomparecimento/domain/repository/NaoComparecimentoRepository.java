package com.g4.odontohub.confirmacao.naocomparecimento.domain.repository;

import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimento;

import java.util.List;

/** Porta de persistência do contexto de Não Comparecimento (camada de domínio). */
public interface NaoComparecimentoRepository {

    void salvar(NaoComparecimento registro);

    NaoComparecimento buscarPorAgendamento(Long agendamentoId);

    List<NaoComparecimento> listarPorPaciente(String paciente);

    List<NaoComparecimento> todos();

    long proximoId();
}
