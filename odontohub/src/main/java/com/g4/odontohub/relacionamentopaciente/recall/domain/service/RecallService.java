package com.g4.odontohub.relacionamentopaciente.recall.domain.service;

import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallCanceladoPorAgendamentoExistente;
import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallConvertido;
import com.g4.odontohub.relacionamentopaciente.recall.domain.event.RecallDisparado;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.RecallId;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.StatusRecall;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecallService {

    private final Map<Long, Recall> repositorio = new HashMap<>();
    private final Map<String, Long> agendamentoFuturoPorPaciente = new HashMap<>();
    private long nextId = 1;
    private RecallCanceladoPorAgendamentoExistente ultimoCancelamento;

    /** Gatilhos temporais dinâmicos por tipo de procedimento (configuráveis). */
    private int prazoParaRetorno(String procedimento) {
        return switch (procedimento == null ? "" : procedimento.trim().toUpperCase()) {
            case "PROFILAXIA" -> 180;
            case "IMPLANTE" -> 45;
            case "CIRURGIA" -> 1;
            case "ORTODONTIA" -> 30;
            default -> 90;
        };
    }

    public Recall dispararRecall(String paciente, String procedimentoGatilho) {
        int dias = prazoParaRetorno(procedimentoGatilho);
        RecallId id = new RecallId(nextId++);
        Recall recall = new Recall(id, paciente, procedimentoGatilho, dias);
        repositorio.put(id.id(), recall);
        DomainEventPublisher.publish(new RecallDisparado(id, paciente, procedimentoGatilho, dias));
        return recall;
    }

    public Recall inserirNaFila(String paciente) {
        return dispararRecall(paciente, "Profilaxia");
    }

    public void registrarAgendamentoFuturo(String paciente, Long agendamentoId) {
        agendamentoFuturoPorPaciente.put(paciente, agendamentoId);
    }

    public void verificarECancelarSeJaAgendado(String paciente) {
        Long agendamentoId = agendamentoFuturoPorPaciente.get(paciente);
        if (agendamentoId == null) {
            return;
        }
        recallNaFila(paciente).ifPresent(recall -> {
            recall.cancelar();
            ultimoCancelamento = new RecallCanceladoPorAgendamentoExistente(recall.getId(), agendamentoId);
            DomainEventPublisher.publish(ultimoCancelamento);
        });
    }

    public Recall registrarConversao(String paciente, Long agendamentoGeradoId) {
        Recall recall = recallNaFila(paciente)
                .orElseThrow(() -> new IllegalStateException("Paciente não está na fila de recall: " + paciente));
        recall.converter();
        DomainEventPublisher.publish(new RecallConvertido(recall.getId(), agendamentoGeradoId));
        return recall;
    }

    public boolean estaNaFila(String paciente) {
        return recallNaFila(paciente).isPresent();
    }

    public Recall buscarPorPaciente(String paciente) {
        return repositorio.values().stream()
                .filter(r -> r.getPaciente().equals(paciente))
                .reduce((primeiro, segundo) -> segundo)
                .orElseThrow(() -> new IllegalArgumentException("Recall não encontrado para: " + paciente));
    }

    public RecallCanceladoPorAgendamentoExistente getUltimoCancelamento() {
        return ultimoCancelamento;
    }

    private Optional<Recall> recallNaFila(String paciente) {
        return repositorio.values().stream()
                .filter(r -> r.getPaciente().equals(paciente))
                .filter(r -> r.getStatus() == StatusRecall.NA_FILA)
                .findFirst();
    }
}
