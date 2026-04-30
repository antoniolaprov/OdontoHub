package com.g4.odontohub.agendamento.domain.service;

import com.g4.odontohub.agendamento.domain.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AgendamentoService {

    private final Map<AgendamentoId, Agendamento> agendamentos = new HashMap<>();
    private long nextId = 1L;

    public Agendamento registrarAgendamento(PacienteId pacienteId, DentistaId dentistaId,
                                            LocalDateTime dataHora, boolean pacienteTemPlanoAtivo,
                                            boolean pacienteInadimplente) {
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é permitido registrar agendamentos em datas passadas");
        }
        if (pacienteInadimplente) {
            throw new IllegalArgumentException("Paciente restrito: possui parcelas em atraso há mais de 30 dias");
        }
        if (existeConflitoDeHorario(dentistaId, dataHora)) {
            throw new IllegalArgumentException("Conflito de horário: o dentista já possui um agendamento neste horário");
        }

        TipoAtendimento tipo = pacienteTemPlanoAtivo ? TipoAtendimento.RETORNO : TipoAtendimento.CONSULTA;
        AgendamentoId id = new AgendamentoId(nextId++);
        Agendamento agendamento = new Agendamento(id, pacienteId, dentistaId, dataHora, tipo);
        agendamentos.put(id, agendamento);
        return agendamento;
    }

    public void confirmarAgendamento(AgendamentoId id, String responsavel) {
        agendamentos.get(id).confirmar(responsavel);
    }

    public void cancelarAgendamento(AgendamentoId id, String motivo, String responsavel) {
        agendamentos.get(id).cancelar(motivo, responsavel);
    }

    public void remarcarAgendamento(AgendamentoId id, LocalDateTime novaDataHora, String responsavel) {
        obterAgendamento(id).remarcar(novaDataHora, responsavel);
    }

    public void enviarLembreteConsulta(AgendamentoId id, String canal) {
        Agendamento agendamento = obterAgendamento(id);
        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new IllegalArgumentException("Nao e permitido enviar lembrete para agendamento cancelado");
        }
        if (agendamento.getStatus() != StatusAgendamento.CONFIRMADO) {
            throw new IllegalArgumentException("Nao e permitido enviar lembrete para agendamento nao confirmado");
        }
        if (agendamento.getDataUltimoLembrete() != null
                && agendamento.getDataUltimoLembrete().isAfter(LocalDateTime.now().minusHours(24))) {
            throw new IllegalArgumentException("Ja existe lembrete enviado para este agendamento no periodo configurado");
        }
        agendamento.enviarLembrete(canal);
    }

    public void registrarConfirmacaoPaciente(AgendamentoId id) {
        obterAgendamento(id).registrarConfirmacaoPaciente();
    }

    public void registrarRecusaPaciente(AgendamentoId id, String motivo, String responsavel) {
        obterAgendamento(id).registrarRecusaPaciente(motivo, responsavel);
    }

    public void marcarNaoComparecimento(AgendamentoId id, String responsavel) {
        marcarNaoComparecimento(id, responsavel, LocalDateTime.now());
    }

    public void marcarNaoComparecimento(AgendamentoId id, String responsavel, LocalDateTime dataHoraAtual) {
        obterAgendamento(id).registrarNaoComparecimento(responsavel, dataHoraAtual);
    }

    public Agendamento obterAgendamento(AgendamentoId id) {
        Agendamento agendamento = agendamentos.get(id);
        if (agendamento == null) {
            throw new IllegalArgumentException("Agendamento nao encontrado");
        }
        return agendamento;
    }

    public Agendamento obterAgendamentoPorPacienteEData(PacienteId pacienteId, LocalDateTime dataHora) {
        return agendamentos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .filter(a -> a.getDataHora().equals(dataHora))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Agendamento nao encontrado"));
    }

    public boolean existeConflitoDeHorario(DentistaId dentistaId, LocalDateTime dataHora) {
        return agendamentos.values().stream()
                .filter(a -> a.getDentistaId().equals(dentistaId))
                .filter(a -> a.getStatus() != StatusAgendamento.CANCELADO)
                .anyMatch(a -> a.getDataHora().equals(dataHora));
    }

    public long contarNoShowsRecentes(PacienteId pacienteId, LocalDateTime dataReferencia) {
        LocalDateTime inicioJanela = dataReferencia.minusMonths(6);
        return agendamentos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .filter(a -> a.getStatus() == StatusAgendamento.NAO_COMPARECEU)
                .filter(a -> !a.getDataHora().isBefore(inicioJanela))
                .filter(a -> !a.getDataHora().isAfter(dataReferencia))
                .count();
    }

    public ResumoOcorrenciasAgenda consultarResumoOcorrencias(PacienteId pacienteId) {
        long quantidadeNoShows = agendamentos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .filter(a -> a.getStatus() == StatusAgendamento.NAO_COMPARECEU)
                .count();
        long quantidadeCancelamentos = agendamentos.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .filter(a -> a.getStatus() == StatusAgendamento.CANCELADO)
                .count();
        return new ResumoOcorrenciasAgenda(quantidadeNoShows, quantidadeCancelamentos);
    }

    public record ResumoOcorrenciasAgenda(long quantidadeNoShows, long quantidadeCancelamentos) {}
}
