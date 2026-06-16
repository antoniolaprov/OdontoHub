package com.g4.odontohub.confirmacao.naocomparecimento.application;

import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimento;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.repository.NaoComparecimentoRepository;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.service.NaoComparecimentoService;
import com.g4.odontohub.confirmacao.naocomparecimento.infrastructure.persistence.InMemoryNaoComparecimentoRepository;

import java.time.LocalDate;

public class NaoComparecimentoApplicationService {

    private final NaoComparecimentoService service;

    public NaoComparecimentoApplicationService() {
        this(new InMemoryNaoComparecimentoRepository());
    }

    public NaoComparecimentoApplicationService(NaoComparecimentoRepository repositorio) {
        this.service = new NaoComparecimentoService(repositorio);
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
