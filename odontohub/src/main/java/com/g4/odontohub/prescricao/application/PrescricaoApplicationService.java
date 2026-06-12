package com.g4.odontohub.prescricao.application;

import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;
import com.g4.odontohub.prescricao.domain.repository.PrescricaoRepository;
import com.g4.odontohub.prescricao.domain.service.PrescricaoService;
import com.g4.odontohub.prescricao.infrastructure.persistence.InMemoryPrescricaoRepository;

import java.time.LocalDate;
import java.util.List;

public class PrescricaoApplicationService {

    private final PrescricaoService service;

    public PrescricaoApplicationService() {
        this(new InMemoryPrescricaoRepository());
    }

    public PrescricaoApplicationService(PrescricaoRepository repositorio) {
        this.service = new PrescricaoService(repositorio);
    }

    public Prescricao registrarPrescricao(String paciente, String dentista, List<ItemPrescricao> itens,
                                          String observacoes) {
        return service.registrarPrescricao(paciente, dentista, itens, observacoes, LocalDate.now());
    }

    public Prescricao registrarPrescricaoComData(String paciente, String dentista, List<ItemPrescricao> itens,
                                                 String observacoes, LocalDate data) {
        return service.registrarPrescricao(paciente, dentista, itens, observacoes, data);
    }

    public Prescricao repetirPrescricao(Long prescricaoOrigemId, String dentista) {
        return service.repetirPrescricao(prescricaoOrigemId, dentista);
    }

    public List<Prescricao> listarPorPaciente(String paciente) {
        return service.listarPorPaciente(paciente);
    }

    public List<Prescricao> filtrarPorPeriodoEDentista(String dentista, int mes, int ano) {
        return service.filtrarPorPeriodoEDentista(dentista, mes, ano);
    }
}
