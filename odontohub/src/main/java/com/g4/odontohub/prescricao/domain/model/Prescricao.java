package com.g4.odontohub.prescricao.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Prescricao {
    private final PrescricaoId id;
    private final PrescricaoPacienteId pacienteId;
    private final Prescricaodentista dentistaId;
    private final LocalDate dataPrescricao;
    private String observacoesTerapeuticas;
    private final List<ItemPrescricao> itens;
    private Long prescricaoOrigemId;

    public Prescricao(PrescricaoId id, PrescricaoPacienteId pacienteId,
            Prescricaodentista dentistaId, LocalDate dataPrescricao,
            String observacoesTerapeuticas) {
        this.id = Objects.requireNonNull(id, "ID da prescrição não pode ser nulo");
        this.pacienteId = Objects.requireNonNull(pacienteId, "ID do paciente não pode ser nulo");
        this.dentistaId = Objects.requireNonNull(dentistaId, "ID do dentista não pode ser nulo");
        this.dataPrescricao = Objects.requireNonNull(dataPrescricao, "Data da prescrição não pode ser nula");
        this.observacoesTerapeuticas = observacoesTerapeuticas;
        this.itens = new ArrayList<>();
    }

    public static Prescricao criar(Long pacienteId, Long dentistaId, String observacoes) {
        return new Prescricao(
                PrescricaoId.generate(),
                new PrescricaoPacienteId(pacienteId),
                new Prescricaodentista(dentistaId),
                LocalDate.now(),
                observacoes);
    }

    public void adicionarItem(ItemPrescricao item) {
        this.itens.add(Objects.requireNonNull(item, "Item de prescrição não pode ser nulo"));
    }

    public void adicionarItens(List<ItemPrescricao> novosItens) {
        novosItens.forEach(this::adicionarItem);
    }

    public void setPrescricaoOrigemId(Long prescricaoOrigemId) {
        this.prescricaoOrigemId = prescricaoOrigemId;
    }

    public PrescricaoId getId() {
        return id;
    }

    public PrescricaoPacienteId getPacienteId() {
        return pacienteId;
    }

    public Prescricaodentista getDentistaId() {
        return dentistaId;
    }

    public LocalDate getDataPrescricao() {
        return dataPrescricao;
    }

    public String getObservacoesTerapeuticas() {
        return observacoesTerapeuticas;
    }

    public List<ItemPrescricao> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public Long getPrescricaoOrigemId() {
        return prescricaoOrigemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Prescricao that = (Prescricao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Prescricao{" +
                "id=" + id +
                ", pacienteId=" + pacienteId +
                ", dentistaId=" + dentistaId +
                ", dataPrescricao=" + dataPrescricao +
                ", observacoesTerapeuticas='" + observacoesTerapeuticas + '\'' +
                ", itens=" + itens +
                ", prescricaoOrigemId=" + prescricaoOrigemId +
                '}';
    }
}