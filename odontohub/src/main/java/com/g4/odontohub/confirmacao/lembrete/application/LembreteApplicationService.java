package com.g4.odontohub.confirmacao.lembrete.application;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.confirmacao.lembrete.domain.event.ConsultaConfirmadaPeloPaciente;
import com.g4.odontohub.confirmacao.lembrete.domain.model.Lembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.model.StatusLembrete;
import com.g4.odontohub.confirmacao.lembrete.domain.repository.LembreteRepository;
import com.g4.odontohub.confirmacao.lembrete.domain.service.LembreteService;
import com.g4.odontohub.confirmacao.lembrete.infrastructure.persistence.InMemoryLembreteRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

public class LembreteApplicationService {

    private final LembreteService service;

    public LembreteApplicationService() {
        this(new InMemoryLembreteRepository());
    }

    public LembreteApplicationService(LembreteRepository repositorio) {
        this.service = new LembreteService(repositorio);
    }

    /**
     * Composition root de produção: integra a confirmação do paciente (F16) com o
     * contexto de Agendamento (F1), marcando o agendamento como confirmado a cada
     * {@link ConsultaConfirmadaPeloPaciente}.
     */
    public LembreteApplicationService(LembreteRepository repositorio,
                                      AgendamentoApplicationService agendamentoService) {
        this.service = new LembreteService(repositorio);
        DomainEventPublisher.subscribe(ConsultaConfirmadaPeloPaciente.class, evento ->
                agendamentoService.confirmarPorConfirmacaoDoPaciente(
                        evento.agendamentoId(), "Confirmação do paciente (lembrete)"));
    }

    public Lembrete gerarLembrete(Long agendamentoId, String paciente, String canal) {
        return service.gerarLembrete(agendamentoId, paciente, canal);
    }

    public Lembrete enviar(Long agendamentoId) {
        return service.enviar(agendamentoId);
    }

    public Lembrete confirmar(Long agendamentoId) {
        return service.confirmar(agendamentoId);
    }

    public Lembrete recusar(Long agendamentoId, String motivo) {
        return service.recusar(agendamentoId, motivo);
    }

    public StatusLembrete statusDoAgendamento(Long agendamentoId) {
        return service.statusDoAgendamento(agendamentoId);
    }

    public Lembrete buscarPorAgendamento(Long agendamentoId) {
        return service.buscarPorAgendamento(agendamentoId);
    }
}
