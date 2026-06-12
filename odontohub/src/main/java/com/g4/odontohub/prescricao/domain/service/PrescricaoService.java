package com.g4.odontohub.prescricao.domain.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.g4.odontohub.prescricao.domain.event.PrescricaoRegistrada;
import com.g4.odontohub.prescricao.domain.event.PrescricaoRepetida;
import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;
import com.g4.odontohub.prescricao.domain.model.PrescricaoId;
import com.g4.odontohub.prescricao.domain.model.PrescricaoPacienteId;
import com.g4.odontohub.prescricao.domain.model.Prescricaodentista;

public class PrescricaoService {

    private final Map<PrescricaoId, Prescricao> repositorio = new HashMap<>();
    private final List<PrescricaoRegistrada> eventosRegistrados = new ArrayList<>();
    private final List<PrescricaoRepetida> eventosRepetidos = new ArrayList<>();

    public Prescricao registrarPrescricao(Long pacienteId, Long dentistaId,
            List<ItemPrescricao> medicamentos,
            String observacoes) {
        return registrarPrescricaoComData(pacienteId, dentistaId, medicamentos, observacoes, LocalDate.now());
    }

    public Prescricao registrarPrescricaoComData(Long pacienteId, Long dentistaId,
            List<ItemPrescricao> medicamentos,
            String observacoes,
            LocalDate dataPrescricao) {
        Prescricao prescricao = new Prescricao(
                PrescricaoId.generate(),
                new PrescricaoPacienteId(pacienteId),
                new Prescricaodentista(dentistaId),
                dataPrescricao,
                observacoes);
        prescricao.adicionarItens(medicamentos);

        repositorio.put(prescricao.getId(), prescricao);

        PrescricaoRegistrada evento = new PrescricaoRegistrada(
                prescricao.getId(),
                pacienteId,
                dentistaId,
                dataPrescricao);
        eventosRegistrados.add(evento);

        return prescricao;
    }

    public Prescricao repetirPrescricao(Long prescricaoOrigemId, Long dentistaId) {
        PrescricaoId origemId = new PrescricaoId(prescricaoOrigemId);
        Prescricao prescricaoOrigem = repositorio.get(origemId);

        if (prescricaoOrigem == null) {
            throw new IllegalArgumentException("Prescrição de origem não encontrada: " + prescricaoOrigemId);
        }

        Prescricao novaPrescricao = Prescricao.criar(
                prescricaoOrigem.getPacienteId().getId(),
                dentistaId,
                prescricaoOrigem.getObservacoesTerapeuticas());

        List<ItemPrescricao> itensOriginais = new ArrayList<>(prescricaoOrigem.getItens());
        novaPrescricao.adicionarItens(itensOriginais);
        novaPrescricao.setPrescricaoOrigemId(prescricaoOrigemId);

        repositorio.put(novaPrescricao.getId(), novaPrescricao);

        PrescricaoRepetida evento = new PrescricaoRepetida(
                novaPrescricao.getId(),
                prescricaoOrigemId,
                novaPrescricao.getDataPrescricao());
        eventosRepetidos.add(evento);

        return novaPrescricao;
    }

    public List<Prescricao> listarPorPaciente(Long pacienteId) {
        return repositorio.values().stream()
                .filter(p -> p.getPacienteId().getId().equals(pacienteId))
                .sorted(Comparator.comparing(Prescricao::getDataPrescricao).reversed())
                .collect(Collectors.toList());
    }

    public List<Prescricao> filtrarPorPeriodoEDentista(Long dentistaId, LocalDate dataInicio, LocalDate dataFim) {
        return repositorio.values().stream()
                .filter(p -> p.getDentistaId().getDentistaId().equals(dentistaId))
                .filter(p -> !p.getDataPrescricao().isBefore(dataInicio) && !p.getDataPrescricao().isAfter(dataFim))
                .sorted(Comparator.comparing(Prescricao::getDataPrescricao).reversed())
                .collect(Collectors.toList());
    }

    public Prescricao buscarPorId(PrescricaoId id) {
        return repositorio.get(id);
    }

    public Prescricao buscarPorIdLong(Long id) {
        return repositorio.get(new PrescricaoId(id));
    }

    public List<PrescricaoRegistrada> getEventosRegistrados() {
        return Collections.unmodifiableList(eventosRegistrados);
    }

    public List<PrescricaoRepetida> getEventosRepetidos() {
        return Collections.unmodifiableList(eventosRepetidos);
    }

    public void limpar() {
        repositorio.clear();
        eventosRegistrados.clear();
        eventosRepetidos.clear();
    }
}