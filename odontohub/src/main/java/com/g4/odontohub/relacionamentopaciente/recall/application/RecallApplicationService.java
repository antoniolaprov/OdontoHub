package com.g4.odontohub.relacionamentopaciente.recall.application;

import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallCanceladoPorAgendamentoExistente;
import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallEscalonado;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.NivelPrioridade;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;
import com.g4.odontohub.relacionamentopaciente.recall.domain.service.RecallService;
import com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.InMemoryRecallRepository;

import java.util.List;

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

    public List<Recall> filaPriorizada() {
        return service.filaPriorizada();
    }

    public NivelPrioridade prioridadeDe(String paciente) {
        return service.prioridadeDe(paciente);
    }

    public boolean registrarTentativaContato(String paciente) {
        return service.registrarTentativaContato(paciente);
    }

    public RecallEscalonado getUltimoEscalonamento() {
        return service.getUltimoEscalonamento();
    }

    public double taxaConversao() {
        return service.taxaConversao();
    }
}
