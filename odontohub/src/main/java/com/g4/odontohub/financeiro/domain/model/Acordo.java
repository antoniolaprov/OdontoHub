package com.g4.odontohub.financeiro.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Acordo {

    private final AcordoId id;
    private final AcordoPacienteId pacienteId;
    private final String paciente;
    private final List<ParcelaCobranca> parcelasOriginais;
    private final List<ParcelaCobranca> novasParcelas;
    private final LocalDate dataCriacao;
    private final String justificativa;
    private final double valorTotal;
    private final List<HistoricoNegociacao> historico = new ArrayList<>();
    private StatusAcordo status;
    private boolean inadimplido;
    private boolean multasRetroativasAtivas;

    public Acordo(AcordoId id, String paciente, List<ParcelaCobranca> parcelasOriginais,
                  List<ParcelaCobranca> novasParcelas) {
        this(id, paciente, parcelasOriginais, novasParcelas, "Acordo de pagamento");
    }

    public Acordo(AcordoId id, String paciente, List<ParcelaCobranca> parcelasOriginais,
                  List<ParcelaCobranca> novasParcelas, String justificativa) {
        this.id = id;
        this.pacienteId = new AcordoPacienteId(Integer.toUnsignedLong(paciente.hashCode()));
        this.paciente = paciente;
        this.parcelasOriginais = parcelasOriginais;
        this.novasParcelas = novasParcelas;
        this.dataCriacao = LocalDate.now();
        this.justificativa = justificativa;
        this.valorTotal = parcelasOriginais.stream().mapToDouble(ParcelaCobranca::getValorAtualizado).sum();
        this.status = StatusAcordo.ATIVO;
        this.parcelasOriginais.forEach(ParcelaCobranca::substituir);
    }

    public static Acordo reconstituir(AcordoId id, String paciente, List<ParcelaCobranca> parcelasOriginais,
                                      List<ParcelaCobranca> novasParcelas, boolean inadimplido,
                                      boolean multasRetroativasAtivas) {
        Acordo acordo = new Acordo(id, paciente, new ArrayList<>(), novasParcelas);
        acordo.parcelasOriginais.addAll(parcelasOriginais);
        acordo.inadimplido = inadimplido;
        acordo.multasRetroativasAtivas = multasRetroativasAtivas;
        acordo.status = inadimplido ? StatusAcordo.INADIMPLIDO : StatusAcordo.ATIVO;
        return acordo;
    }

    public void inadimplir() {
        this.inadimplido = true;
        this.status = StatusAcordo.INADIMPLIDO;
        this.multasRetroativasAtivas = true;
    }

    public void cancelar(String justificativa, String responsavel) {
        StatusAcordo anterior = status;
        this.status = StatusAcordo.CANCELADO;
        this.inadimplido = false;
        this.multasRetroativasAtivas = true;
        novasParcelas.forEach(ParcelaCobranca::cancelar);
        parcelasOriginais.forEach(p -> p.vencer(p.getDiasAtraso(), p.getMulta(), p.getJuros()));
        historico.add(new HistoricoNegociacao(responsavel, LocalDate.now(), anterior, status, justificativa));
    }

    public void alterarStatus(StatusAcordo novoStatus, String justificativa, String responsavel) {
        StatusAcordo anterior = status;
        this.status = novoStatus;
        this.inadimplido = novoStatus == StatusAcordo.INADIMPLIDO;
        historico.add(new HistoricoNegociacao(responsavel, LocalDate.now(), anterior, novoStatus, justificativa));
    }

    public void vencerPrimeiraNovaParcela(int diasAtraso, double multa, double juros) {
        if (novasParcelas.isEmpty()) {
            throw new IllegalStateException("Acordo sem parcelas geradas");
        }
        novasParcelas.get(0).vencer(diasAtraso, multa, juros);
    }

    public boolean possuiParcelaGeradaVencida() {
        return novasParcelas.stream().anyMatch(p -> p.getStatus() == StatusParcela.VENCIDA);
    }

    public AcordoId getId() { return id; }
    public AcordoPacienteId getPacienteId() { return pacienteId; }
    public String getPaciente() { return paciente; }
    public List<ParcelaCobranca> getParcelasOriginais() { return parcelasOriginais; }
    public List<ParcelaCobranca> getNovasParcelas() { return novasParcelas; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public String getJustificativa() { return justificativa; }
    public double getValorTotal() { return valorTotal; }
    public StatusAcordo getStatus() { return status; }
    public List<HistoricoNegociacao> getHistorico() { return historico; }
    public boolean isInadimplido() { return inadimplido; }
    public boolean isMultasRetroativasAtivas() { return multasRetroativasAtivas; }
}
