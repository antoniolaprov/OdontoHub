package com.g4.odontohub.financeiro.domain.model;

import java.time.LocalDate;
import java.util.List;

public class Acordo {

    private final AcordoId id;
    private final Long pacienteId;
    private final List<Parcela> parcelasOriginais;
    private final List<Parcela> novasParcelas;
    private final LocalDate dataCriacao;
    private boolean inadimplido;

    public Acordo(Long id, Long pacienteId, List<Parcela> parcelasOriginais, List<Parcela> novasParcelas) {
        this.id = new AcordoId(id);
        this.pacienteId = pacienteId;
        this.parcelasOriginais = parcelasOriginais;
        this.novasParcelas = novasParcelas;
        this.dataCriacao = LocalDate.now();
        this.inadimplido = false;

        parcelasOriginais.forEach(Parcela::substituir);
    }

    public void registrarInadimplencia() {
        this.inadimplido = true;
        // multas retroativas voltam a valer — recalcula nas parcelas originais
        parcelasOriginais.forEach(Parcela::calcularJurosEMulta);
    }

    public AcordoId getId() { return id; }
    public Long getPacienteId() { return pacienteId; }
    public List<Parcela> getParcelasOriginais() { return parcelasOriginais; }
    public List<Parcela> getNovasParcelas() { return novasParcelas; }
    public boolean isInadimplido() { return inadimplido; }
}