package com.g4.odontohub.inadimplencia.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Parcela {

    private static final BigDecimal TAXA_JUROS_DIARIA = new BigDecimal("0.001"); // 0,1% ao dia
    private static final BigDecimal TAXA_MULTA = new BigDecimal("0.02");         // 2% fixo

    private final String id;
    private final String pacienteId;
    private String acordoId;
    private BigDecimal valorOriginal;
    private LocalDate vencimento;
    private StatusParcela status;
    private boolean geradaPorAcordo;
    private String parcelaOriginalId; // referência quando substituída

    public Parcela(String pacienteId, BigDecimal valorOriginal, LocalDate vencimento) {
        this.id = UUID.randomUUID().toString();
        this.pacienteId = pacienteId;
        this.valorOriginal = valorOriginal;
        this.vencimento = vencimento;
        this.status = StatusParcela.EM_ABERTO;
        this.geradaPorAcordo = false;
    }

    public void verificarEMarcarVencida() {
        if (status == StatusParcela.EM_ABERTO && LocalDate.now().isAfter(vencimento)) {
            this.status = StatusParcela.VENCIDA;
        }
    }

    public long getDiasAtraso() {
        if (!isVencida()) return 0;
        return ChronoUnit.DAYS.between(vencimento, LocalDate.now());
    }

    public BigDecimal getJuros() {
        long dias = getDiasAtraso();
        if (dias <= 0) return BigDecimal.ZERO;
        return valorOriginal.multiply(TAXA_JUROS_DIARIA)
                .multiply(BigDecimal.valueOf(dias))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMulta() {
        if (!isVencida()) return BigDecimal.ZERO;
        return valorOriginal.multiply(TAXA_MULTA).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getValorAtualizado() {
        return valorOriginal.add(getJuros()).add(getMulta());
    }

    public BigDecimal getValorAtualizadoNaData(LocalDate data) {
        long dias = ChronoUnit.DAYS.between(vencimento, data);
        if (dias <= 0) return valorOriginal;
        BigDecimal juros = valorOriginal.multiply(TAXA_JUROS_DIARIA)
                .multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal multa = valorOriginal.multiply(TAXA_MULTA).setScale(2, RoundingMode.HALF_UP);
        return valorOriginal.add(juros).add(multa);
    }

    public boolean isVencida() {
        return status == StatusParcela.VENCIDA;
    }

    public void substituir() { this.status = StatusParcela.SUBSTITUIDA; }
    public void cancelar()   { this.status = StatusParcela.CANCELADA; }
    public void pagar()      { this.status = StatusParcela.PAGA; }
    public void reativarComoVencida() { this.status = StatusParcela.VENCIDA; }

    public void vincularAcordo(String acordoId) {
        this.acordoId = acordoId;
        this.geradaPorAcordo = true;
    }

    // Getters
    public String getId() { return id; }
    public String getPacienteId() { return pacienteId; }
    public String getAcordoId() { return acordoId; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public LocalDate getVencimento() { return vencimento; }
    public StatusParcela getStatus() { return status; }
    public boolean isGeradaPorAcordo() { return geradaPorAcordo; }
    public String getParcelaOriginalId() { return parcelaOriginalId; }
    public void setParcelaOriginalId(String id) { this.parcelaOriginalId = id; }
}
