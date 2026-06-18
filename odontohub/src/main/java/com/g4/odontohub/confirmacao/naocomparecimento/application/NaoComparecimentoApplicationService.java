package com.g4.odontohub.confirmacao.naocomparecimento.application;

import com.g4.odontohub.confirmacao.naocomparecimento.domain.event.FaltaRegistrada;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.event.PacienteReincidente;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimento;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.repository.NaoComparecimentoRepository;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.service.NaoComparecimentoService;
import com.g4.odontohub.confirmacao.naocomparecimento.infrastructure.persistence.InMemoryNaoComparecimentoRepository;
import com.g4.odontohub.relacionamentopaciente.churn.application.ChurnApplicationService;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;

public class NaoComparecimentoApplicationService {

    private final NaoComparecimentoService service;

    public NaoComparecimentoApplicationService() {
        this(new InMemoryNaoComparecimentoRepository());
    }

    public NaoComparecimentoApplicationService(NaoComparecimentoRepository repositorio) {
        this.service = new NaoComparecimentoService(repositorio);
    }

    /**
     * Composition root de produção: integra o não comparecimento (F17) com a análise
     * de churn/absenteísmo (F11). Cada falta injustificada conta como no-show no churn;
     * ao atingir o limite de reincidência, o paciente é classificado em zona de risco.
     */
    public NaoComparecimentoApplicationService(NaoComparecimentoRepository repositorio,
                                               ChurnApplicationService churnService) {
        this.service = new NaoComparecimentoService(repositorio);
        DomainEventPublisher.subscribe(FaltaRegistrada.class, evento -> {
            if (!evento.justificada()) {
                churnService.registrarNoShow(evento.paciente());
            }
        });
        DomainEventPublisher.subscribe(PacienteReincidente.class, evento ->
                churnService.classificarManualmente(evento.paciente(), "ZONA_DE_RISCO"));
    }

    public NaoComparecimento registrarFalta(Long agendamentoId, String paciente, LocalDate dataConsulta,
                                            boolean justificada, String justificativa) {
        return service.registrarFalta(agendamentoId, paciente, dataConsulta, justificada, justificativa);
    }

    public int faltasInjustificadasDe(String paciente) {
        return service.faltasInjustificadasDe(paciente);
    }

    public int totalFaltasDe(String paciente) {
        return service.totalFaltasDe(paciente);
    }

    public boolean pacienteReincidente(String paciente) {
        return service.pacienteReincidente(paciente);
    }
}
