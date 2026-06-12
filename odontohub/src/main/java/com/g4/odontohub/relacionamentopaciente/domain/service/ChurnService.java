package com.g4.odontohub.relacionamentopaciente.domain.service;

import com.g4.odontohub.relacionamentopaciente.domain.event.PacienteClassificadoComoChurn;
import com.g4.odontohub.relacionamentopaciente.domain.event.PacienteEntrandoZonaDeRisco;
import com.g4.odontohub.relacionamentopaciente.domain.event.PacienteReativado;
import com.g4.odontohub.relacionamentopaciente.domain.model.AnaliseChurn;
import com.g4.odontohub.relacionamentopaciente.domain.model.StatusChurn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChurnService {

    public List<Object> recalcularStatusChurn(AnaliseChurn analiseChurn,
                                              boolean possuiAgendamentosFuturos,
                                              int mesesSemRetorno,
                                              boolean planoAtivo) {
        List<Object> eventos = new ArrayList<>();

        if (planoAtivo && !possuiAgendamentosFuturos && mesesSemRetorno >= 13) {
            analiseChurn.atualizarStatus(StatusChurn.EVADIDO);
            eventos.add(new PacienteClassificadoComoChurn(
                    analiseChurn.getId(),
                    analiseChurn.getPacienteId().getPacienteId()));
            return eventos;
        }

        if (planoAtivo && mesesSemRetorno >= 6) {
            analiseChurn.atualizarStatus(StatusChurn.ZONA_DE_RISCO);
            eventos.add(new PacienteEntrandoZonaDeRisco(
                    analiseChurn.getId(),
                    analiseChurn.getPacienteId().getPacienteId(),
                    mesesSemRetorno));
            return eventos;
        }

        analiseChurn.atualizarStatus(StatusChurn.ATIVO);
        return eventos;
    }

    public PacienteReativado registrarNovoAgendamento(AnaliseChurn analiseChurn, LocalDate dataNovoAgendamento) {
        analiseChurn.registrarReativacao(dataNovoAgendamento);
        return new PacienteReativado(analiseChurn.getId(), analiseChurn.getPacienteId().getPacienteId());
    }

    public double calcularReceitaPerdida(Long dentistaId, int horasDeAgendaOciosa, double valorMedioHora) {
        return horasDeAgendaOciosa * valorMedioHora;
    }

    public void registrarCancelamentoComMotivo(AnaliseChurn analiseChurn, String motivoCancelamento) {
        if (motivoCancelamento == null || motivoCancelamento.isBlank()) {
            throw new IllegalArgumentException("A categoria do motivo de cancelamento é obrigatória");
        }
        analiseChurn.registrarMotivoCancelamento(motivoCancelamento);
    }
}
