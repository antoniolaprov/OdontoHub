package com.g4.odontohub.medicamento.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Medicamento {

    private final MedicamentoId id;
    private final String nomeComercial;
    private final String principioAtivo;
    private final String categoriaTerapeutica;
    private String classeFarmacologica;
    private StatusMedicamento status;
    private final List<String> posologiasPadrao = new ArrayList<>();
    private final List<String> contraindicacoes = new ArrayList<>();

    public Medicamento(MedicamentoId id, String nomeComercial, String principioAtivo,
                       String categoriaTerapeutica, String classeFarmacologica) {
        this.id = id;
        this.nomeComercial = nomeComercial;
        this.principioAtivo = principioAtivo;
        this.categoriaTerapeutica = categoriaTerapeutica;
        this.classeFarmacologica = classeFarmacologica;
        this.status = StatusMedicamento.ATIVO;
    }

    public void inativar() {
        this.status = StatusMedicamento.INATIVO;
    }

    public boolean estaAtivo() {
        return status == StatusMedicamento.ATIVO;
    }

    public void adicionarPosologiaPadrao(String posologia) {
        this.posologiasPadrao.add(posologia);
    }

    public void adicionarContraindicacao(String contraindicacao) {
        this.contraindicacoes.add(contraindicacao);
    }

    public MedicamentoId getId() { return id; }
    public String getNomeComercial() { return nomeComercial; }
    public String getPrincipioAtivo() { return principioAtivo; }
    public String getCategoriaTerapeutica() { return categoriaTerapeutica; }
    public String getClasseFarmacologica() { return classeFarmacologica; }
    public StatusMedicamento getStatus() { return status; }
    public List<String> getPosologiasPadrao() { return Collections.unmodifiableList(posologiasPadrao); }
    public List<String> getContraindicacoes() { return Collections.unmodifiableList(contraindicacoes); }
}
