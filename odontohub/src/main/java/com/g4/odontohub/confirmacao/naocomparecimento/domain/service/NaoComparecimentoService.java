package com.g4.odontohub.confirmacao.naocomparecimento.domain.service;

import com.g4.odontohub.confirmacao.naocomparecimento.domain.event.FaltaRegistrada;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.event.PacienteReincidente;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimento;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimentoId;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.repository.NaoComparecimentoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;

/**
 * F14 — Registro de Não Comparecimento (No-Show).
 * Registra ausências, impede registros para consultas futuras ou duplicados,
 * e classifica pacientes reincidentes a partir de faltas injustificadas.
 */
public class NaoComparecimentoService {

    /** Faltas injustificadas a partir das quais o paciente é considerado reincidente. */
    private static final int LIMITE_REINCIDENCIA = 3;

    private final NaoComparecimentoRepository repositorio;

    public NaoComparecimentoService(NaoComparecimentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public NaoComparecimento registrarFalta(Long agendamentoId, String paciente, LocalDate dataConsulta,
                                            boolean justificada, String justificativa) {
        if (dataConsulta != null && dataConsulta.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Não é possível registrar falta de uma consulta futura");
        }
        if (repositorio.buscarPorAgendamento(agendamentoId) != null) {
            throw new IllegalStateException("Já existe um registro de não comparecimento para este agendamento");
        }
        if (justificada && (justificativa == null || justificativa.isBlank())) {
            throw new IllegalArgumentException("A justificativa é obrigatória para faltas justificadas");
        }
        NaoComparecimentoId id = new NaoComparecimentoId(repositorio.proximoId());
        NaoComparecimento registro = new NaoComparecimento(
                id, agendamentoId, paciente, dataConsulta, justificada, justificativa);
        repositorio.salvar(registro);
        DomainEventPublisher.publish(new FaltaRegistrada(id, agendamentoId, paciente, justificada));

        if (pacienteReincidente(paciente)) {
            DomainEventPublisher.publish(new PacienteReincidente(paciente, faltasInjustificadasDe(paciente)));
        }
        return registro;
    }

    public int faltasInjustificadasDe(String paciente) {
        return (int) repositorio.listarPorPaciente(paciente).stream()
                .filter(NaoComparecimento::contaParaReincidencia)
                .count();
    }

    public int totalFaltasDe(String paciente) {
        return repositorio.listarPorPaciente(paciente).size();
    }

    public boolean pacienteReincidente(String paciente) {
        return faltasInjustificadasDe(paciente) >= LIMITE_REINCIDENCIA;
    }
}
