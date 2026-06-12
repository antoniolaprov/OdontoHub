package com.g4.odontohub.relacionamentopaciente.recall.application;

import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallCanceladoPorAgendamentoExistente;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;
import com.g4.odontohub.relacionamentopaciente.recall.domain.service.RecallService;
import com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.InMemoryRecallRepository;

public class RecallApplicationService {

    private final RecallService service;

    public RecallApplicationService() {
        this(new InMemoryRecallRepository());
    }

    public RecallApplicationService(RecallRepository repositorio) {
        this.service = new RecallService(repositorio);
    }

    public Recall processarGatilhoRecall(String paciente, String procedimento) {
        return service.dispararRecall(paciente, procedimento);
    }

    public Recall inserirNaFila(String paciente) {
        return service.inserirNaFila(paciente);
    }

    public void registrarAgendamentoFuturo(String paciente, Long agendamentoId) {
        service.registrarAgendamentoFuturo(paciente, agendamentoId);
    }

    public void verificarSobreposicao(String paciente) {
        service.verificarECancelarSeJaAgendado(paciente);
    }

    public Recall registrarConversao(String paciente, Long agendamentoId) {
        return service.registrarConversao(paciente, agendamentoId);
    }

    public boolean estaNaFila(String paciente) {
        return service.estaNaFila(paciente);
    }

    public Recall buscarPorPaciente(String paciente) {
        return service.buscarPorPaciente(paciente);
    }

    public RecallCanceladoPorAgendamentoExistente getUltimoCancelamento() {
        return service.getUltimoCancelamento();
    }
}
