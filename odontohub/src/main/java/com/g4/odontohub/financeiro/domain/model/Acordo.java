package com.g4.odontohub.financeiro.domain.model;

import java.util.List;

public class Acordo {

    private final AcordoId id;
    private final String paciente;
    private final List<ParcelaCobranca> parcelasOriginais;
    private final List<ParcelaCobranca> novasParcelas;
    private boolean inadimplido;
    private boolean multasRetroativasAtivas;

    public Acordo(AcordoId id, String paciente, List<ParcelaCobranca> parcelasOriginais,
                  List<ParcelaCobranca> novasParcelas) {
        this.id = id;
        this.paciente = paciente;
        this.parcelasOriginais = parcelasOriginais;
        this.novasParcelas = novasParcelas;
        // Ao firmar o acordo, as parcelas originais passam a "Substituídas".
        this.parcelasOriginais.forEach(ParcelaCobranca::substituir);
    }

    public void inadimplir() {
        this.inadimplido = true;
        // Se o acordo é inadimplido, as multas retroativas originais voltam a valer.
        this.multasRetroativasAtivas = true;
    }

    public AcordoId getId() { return id; }
    public String getPaciente() { return paciente; }
    public List<ParcelaCobranca> getParcelasOriginais() { return parcelasOriginais; }
    public List<ParcelaCobranca> getNovasParcelas() { return novasParcelas; }
    public boolean isInadimplido() { return inadimplido; }
    public boolean isMultasRetroativasAtivas() { return multasRetroativasAtivas; }
}
