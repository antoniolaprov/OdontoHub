package com.g4.odontohub.confirmacao.lembrete.domain.service;

import com.g4.odontohub.confirmacao.lembrete.domain.event.ConsultaConfirmadaPeloPaciente;
import com.g4.odontohub.confirmacao.lembrete.domain.event.LembreteEnviado;
import com.g4.odontohub.confirmacao.lembrete.domain.model.CanalLembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.model.Lembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.model.LembreteId;
import com.g4.odontohub.confirmacao.lembrete.domain.model.StatusLembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.repository.LembreteRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;

/**
 * F16 — Confirmação e Lembretes de Consulta.
 * Gera, envia e registra a resposta do paciente a lembretes de consulta.
 */
public class LembreteService {

    private final LembreteRepository repositorio;

    public LembreteService(LembreteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Lembrete gerarLembrete(Long agendamentoId, String paciente, String canalLabel) {
        if (repositorio.buscarPorAgendamento(agendamentoId) != null) {
            throw new IllegalStateException("Já existe um lembrete para este agendamento");
        }
        CanalLembrete canal = CanalLembrete.fromLabel(canalLabel);
        LembreteId id = new LembreteId(repositorio.proximoId());
        Lembrete lembrete = new Lembrete(id, agendamentoId, paciente, canal);
        repositorio.salvar(lembrete);
        return lembrete;
    }

    public Lembrete enviar(Long agendamentoId) {
        Lembrete lembrete = exigirLembrete(agendamentoId);
        lembrete.enviar(LocalDate.now());
        repositorio.salvar(lembrete);
        DomainEventPublisher.publish(new LembreteEnviado(lembrete.getId(), agendamentoId, lembrete.getCanal().name()));
        return lembrete;
    }

    public Lembrete confirmar(Long agendamentoId) {
        Lembrete lembrete = exigirLembrete(agendamentoId);
        lembrete.confirmar();
        repositorio.salvar(lembrete);
        DomainEventPublisher.publish(
                new ConsultaConfirmadaPeloPaciente(lembrete.getId(), agendamentoId, lembrete.getPaciente()));
        return lembrete;
    }

    public Lembrete recusar(Long agendamentoId, String motivo) {
        Lembrete lembrete = exigirLembrete(agendamentoId);
        lembrete.recusar(motivo);
        repositorio.salvar(lembrete);
        return lembrete;
    }

    public StatusLembrete statusDoAgendamento(Long agendamentoId) {
        return exigirLembrete(agendamentoId).getStatus();
    }

    public Lembrete buscarPorAgendamento(Long agendamentoId) {
        return exigirLembrete(agendamentoId);
    }

    private Lembrete exigirLembrete(Long agendamentoId) {
        Lembrete lembrete = repositorio.buscarPorAgendamento(agendamentoId);
        if (lembrete == null) {
            throw new IllegalArgumentException("Lembrete não encontrado para o agendamento " + agendamentoId);
        }
        return lembrete;
    }
}
