package com.g4.odontohub.prescricao.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Prescricao {

    private final PrescricaoId id;
    private final String paciente;
    private final String dentista;
    private final LocalDate dataPrescricao;
    private String observacoesTerapeuticas;
    private final List<ItemPrescricao> itens;
    private final Long prescricaoOrigemId;

    public Prescricao(PrescricaoId id, String paciente, String dentista, LocalDate dataPrescricao,
                      List<ItemPrescricao> itens, Long prescricaoOrigemId) {
        this.id = id;
        this.paciente = paciente;
        this.dentista = dentista;
        this.dataPrescricao = dataPrescricao;
        this.itens = new ArrayList<>(itens);
        this.prescricaoOrigemId = prescricaoOrigemId;
    }

    public void definirObservacoes(String observacoes) {
        this.observacoesTerapeuticas = observacoes;
    }

    public boolean contemMedicamento(String nomeMedicamento) {
        return itens.stream().anyMatch(i -> i.nomeMedicamento().contains(nomeMedicamento));
    }

    public PrescricaoId getId() { return id; }
    public String getPaciente() { return paciente; }
    public String getDentista() { return dentista; }
    public LocalDate getDataPrescricao() { return dataPrescricao; }
    public String getObservacoesTerapeuticas() { return observacoesTerapeuticas; }
    public List<ItemPrescricao> getItens() { return Collections.unmodifiableList(itens); }
    public Long getPrescricaoOrigemId() { return prescricaoOrigemId; }
}
