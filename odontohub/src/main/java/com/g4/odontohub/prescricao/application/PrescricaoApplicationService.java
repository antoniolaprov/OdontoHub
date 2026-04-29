package com.g4.odontohub.prescricao.application;

import java.time.LocalDate;
import java.util.List;

import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;
import com.g4.odontohub.prescricao.domain.service.PrescricaoService;

public class PrescricaoApplicationService {

    private final PrescricaoService prescricaoService;

    public PrescricaoApplicationService() {
        this.prescricaoService = new PrescricaoService();
    }

    public PrescricaoApplicationService(PrescricaoService prescricaoService) {
        this.prescricaoService = prescricaoService;
    }

    public Prescricao registrarPrescricao(Long pacienteId, Long dentistaId,
            List<ItemPrescricao> medicamentos,
            String observacoes) {
        return prescricaoService.registrarPrescricao(pacienteId, dentistaId, medicamentos, observacoes);
    }

    public Prescricao registrarPrescricaoComData(Long pacienteId, Long dentistaId,
            List<ItemPrescricao> medicamentos,
            String observacoes,
            LocalDate dataPrescricao) {
        return prescricaoService.registrarPrescricaoComData(pacienteId, dentistaId, medicamentos, observacoes,
                dataPrescricao);
    }

    public Prescricao repetirPrescricao(Long prescricaoOrigemId, Long dentistaId) {
        return prescricaoService.repetirPrescricao(prescricaoOrigemId, dentistaId);
    }

    public List<Prescricao> listarPorPaciente(Long pacienteId) {
        return prescricaoService.listarPorPaciente(pacienteId);
    }

    public List<Prescricao> filtrarPorPeriodoEDentista(Long dentistaId, LocalDate dataInicio, LocalDate dataFim) {
        return prescricaoService.filtrarPorPeriodoEDentista(dentistaId, dataInicio, dataFim);
    }

    public Prescricao buscarPorId(Long id) {
        return prescricaoService.buscarPorIdLong(id);
    }
}