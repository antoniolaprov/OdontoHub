package com.g4.odontohub.inadimplencia.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Acordo {

    private final String id;
    private final String pacienteId;
    private StatusAcordo status;
    private final BigDecimal valorTotal;
    private final int numeroParcelas;
    private final String justificativa;
    private final LocalDate dataCriacao;
    private final List<String> parcelasOriginaisIds;
    private final List<Parcela> parcelasGeradas;
    private final List<HistoricoNegociacao> historico;

    public Acordo(String pacienteId, BigDecimal valorTotal, int numeroParcelas,
                  String justificativa, List<String> parcelasOriginaisIds) {
        this.id = UUID.randomUUID().toString();
        this.pacienteId = pacienteId;
        this.valorTotal = valorTotal;
        this.numeroParcelas = numeroParcelas;
        this.justificativa = justificativa;
        this.dataCriacao = LocalDate.now();
        this.status = StatusAcordo.ATIVO;
        this.parcelasOriginaisIds = new ArrayList<>(parcelasOriginaisIds);
        this.parcelasGeradas = new ArrayList<>();
        this.historico = new ArrayList<>();
    }

    public List<Parcela> gerarParcelas(String pacienteId) {
        BigDecimal valorPorParcela = valorTotal
                .divide(BigDecimal.valueOf(numeroParcelas), 2, java.math.RoundingMode.HALF_UP);
        parcelasGeradas.clear();
        for (int i = 1; i <= numeroParcelas; i++) {
            Parcela p = new Parcela(pacienteId, valorPorParcela,
                    dataCriacao.plusMonths(i));
            p.vincularAcordo(this.id);
            parcelasGeradas.add(p);
        }
        return new ArrayList<>(parcelasGeradas);
    }

    public void marcarInadimplido(String responsavel, String justificativa) {
        StatusAcordo anterior = this.status;
        this.status = StatusAcordo.INADIMPLIDO;
        historico.add(new HistoricoNegociacao(responsavel, anterior, StatusAcordo.INADIMPLIDO, justificativa));
    }

    public void cancelar(String responsavel, String justificativa) {
        StatusAcordo anterior = this.status;
        this.status = StatusAcordo.CANCELADO;
        historico.add(new HistoricoNegociacao(responsavel, anterior, StatusAcordo.CANCELADO, justificativa));
    }

    public void registrarAlteracaoHistorico(String responsavel, StatusAcordo anterior,
                                             StatusAcordo novo, String justificativa) {
        this.status = novo;
        historico.add(new HistoricoNegociacao(responsavel, anterior, novo, justificativa));
    }

    public boolean possuiParcelaVencida() {
        return parcelasGeradas.stream().anyMatch(Parcela::isVencida);
    }

    // Getters
    public String getId() { return id; }
    public String getPacienteId() { return pacienteId; }
    public StatusAcordo getStatus() { return status; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public int getNumeroParcelas() { return numeroParcelas; }
    public String getJustificativa() { return justificativa; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public List<String> getParcelasOriginaisIds() { return parcelasOriginaisIds; }
    public List<Parcela> getParcelasGeradas() { return parcelasGeradas; }
    public List<HistoricoNegociacao> getHistorico() { return historico; }
}
