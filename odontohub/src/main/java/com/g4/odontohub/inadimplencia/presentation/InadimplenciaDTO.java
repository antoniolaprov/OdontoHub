package com.g4.odontohub.inadimplencia.presentation;

import java.math.BigDecimal;

/**
 * DTO de apresentação para exibição na tela de inadimplentes.
 * Não expõe entidades de domínio diretamente.
 */
public class InadimplenciaDTO {

    private String pacienteId;
    private String nomePaciente;
    private boolean pacienteRestrito;
    private boolean destaqueVisual;

    private String parcelaId;
    private BigDecimal valorOriginal;
    private BigDecimal juros;
    private BigDecimal multa;
    private BigDecimal valorAtualizado;
    private long diasAtraso;
    private String vencimento;
    private String statusParcela;

    // Builder estático para facilitar construção
    public static InadimplenciaDTO of(String pacienteId, String nomePaciente,
                                      boolean restrito, boolean destaque,
                                      String parcelaId, BigDecimal valorOriginal,
                                      BigDecimal juros, BigDecimal multa,
                                      BigDecimal valorAtualizado, long diasAtraso,
                                      String vencimento, String statusParcela) {
        InadimplenciaDTO dto = new InadimplenciaDTO();
        dto.pacienteId = pacienteId;
        dto.nomePaciente = nomePaciente;
        dto.pacienteRestrito = restrito;
        dto.destaqueVisual = destaque;
        dto.parcelaId = parcelaId;
        dto.valorOriginal = valorOriginal;
        dto.juros = juros;
        dto.multa = multa;
        dto.valorAtualizado = valorAtualizado;
        dto.diasAtraso = diasAtraso;
        dto.vencimento = vencimento;
        dto.statusParcela = statusParcela;
        return dto;
    }

    // Getters
    public String getPacienteId()       { return pacienteId; }
    public String getNomePaciente()     { return nomePaciente; }
    public boolean isPacienteRestrito() { return pacienteRestrito; }
    public boolean isDestaqueVisual()   { return destaqueVisual; }
    public String getParcelaId()        { return parcelaId; }
    public BigDecimal getValorOriginal(){ return valorOriginal; }
    public BigDecimal getJuros()        { return juros; }
    public BigDecimal getMulta()        { return multa; }
    public BigDecimal getValorAtualizado(){ return valorAtualizado; }
    public long getDiasAtraso()         { return diasAtraso; }
    public String getVencimento()       { return vencimento; }
    public String getStatusParcela()    { return statusParcela; }
}
