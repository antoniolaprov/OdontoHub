package com.g4.odontohub.prescricao.domain.service;

import com.g4.odontohub.prescricao.domain.event.PrescricaoRegistrada;
import com.g4.odontohub.prescricao.domain.event.PrescricaoRepetida;
import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;
import com.g4.odontohub.prescricao.domain.model.PrescricaoId;
import com.g4.odontohub.prescricao.domain.repository.PrescricaoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrescricaoService {

    private final PrescricaoRepository repositorio;

    public PrescricaoService(PrescricaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Prescricao registrarPrescricao(String paciente, String dentista, List<ItemPrescricao> itens,
                                          String observacoes, LocalDate data) {
        PrescricaoId id = new PrescricaoId(repositorio.proximoId());
        Prescricao prescricao = new Prescricao(id, paciente, dentista, data, itens, null);
        if (observacoes != null && !observacoes.isBlank()) {
            prescricao.definirObservacoes(observacoes);
        }
        repositorio.salvar(prescricao);
        DomainEventPublisher.publish(new PrescricaoRegistrada(id, paciente, dentista, data));
        return prescricao;
    }

    public Prescricao repetirPrescricao(Long prescricaoOrigemId, String dentista) {
        Prescricao origem = buscar(prescricaoOrigemId);
        PrescricaoId id = new PrescricaoId(repositorio.proximoId());
        Prescricao nova = new Prescricao(id, origem.getPaciente(), dentista, LocalDate.now(),
                origem.getItens(), prescricaoOrigemId);
        nova.definirObservacoes(origem.getObservacoesTerapeuticas());
        repositorio.salvar(nova);
        DomainEventPublisher.publish(new PrescricaoRepetida(id, prescricaoOrigemId, nova.getDataPrescricao()));
        return nova;
    }

    public List<Prescricao> listarPorPaciente(String paciente) {
        return repositorio.todos().stream()
                .filter(p -> p.getPaciente().equals(paciente))
                .sorted(Comparator.comparing(Prescricao::getDataPrescricao).reversed())
                .collect(Collectors.toList());
    }

    public List<Prescricao> filtrarPorPeriodoEDentista(String dentista, int mes, int ano) {
        return repositorio.todos().stream()
                .filter(p -> p.getDentista().equals(dentista))
                .filter(p -> p.getDataPrescricao().getMonthValue() == mes
                        && p.getDataPrescricao().getYear() == ano)
                .collect(Collectors.toList());
    }

    public Prescricao buscar(Long id) {
        Prescricao prescricao = repositorio.buscar(id);
        if (prescricao == null) {
            throw new IllegalArgumentException("Prescrição não encontrada: " + id);
        }
        return prescricao;
    }
}
