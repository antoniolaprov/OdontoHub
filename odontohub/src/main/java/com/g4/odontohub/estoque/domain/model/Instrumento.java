package com.g4.odontohub.estoque.domain.model;

import java.time.LocalDate;

public class Instrumento {

    private final InstrumentoId id;
    private final String nome;
    private final String categoria;
    private final String codigoIdentificador;
    private StatusInstrumento statusInstrumento;
    private StatusEsterilizacao status;
    private LocalDate dataUltimaEsterilizacao;
    private LocalDate dataVencimento;
    private int prazoValidadeDias;
    private String responsavelEsterilizacao;

    public Instrumento(InstrumentoId id, String nome, String categoria,
                       String codigoIdentificador, int prazoValidadeDias) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.codigoIdentificador = codigoIdentificador;
        this.prazoValidadeDias = prazoValidadeDias;
        this.statusInstrumento = StatusInstrumento.ATIVO;
        this.status = StatusEsterilizacao.CONTAMINADO;
    }

    public void desativar() {
        this.statusInstrumento = StatusInstrumento.INATIVO;
    }

    public void marcarComoEsteril(LocalDate dataEsterilizacao, String responsavel) {
        validarAtivo();
        this.status = StatusEsterilizacao.ESTERIL;
        this.dataUltimaEsterilizacao = dataEsterilizacao;
        this.responsavelEsterilizacao = responsavel;
        this.dataVencimento = dataEsterilizacao.plusDays(prazoValidadeDias);
    }

    public void marcarComoContaminado() {
        validarAtivo();
        this.status = StatusEsterilizacao.CONTAMINADO;
    }

    public void marcarComoVencido() {
        validarAtivo();
        this.status = StatusEsterilizacao.VENCIDO;
    }

    public void recalcularVencimento(int novoPrazoDias) {
        validarAtivo();
        this.prazoValidadeDias = novoPrazoDias;
        if (this.dataUltimaEsterilizacao != null) {
            this.dataVencimento = this.dataUltimaEsterilizacao.plusDays(novoPrazoDias);
        }
    }

    private void validarAtivo() {
        if (this.statusInstrumento == StatusInstrumento.INATIVO) {
            throw new IllegalStateException("Instrumento inativo não pode sofrer operações de esterilização");
        }
    }

    public InstrumentoId getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public String getCodigoIdentificador() { return codigoIdentificador; }
    public StatusInstrumento getStatusInstrumento() { return statusInstrumento; }
    public StatusEsterilizacao getStatus() { return status; }
    public LocalDate getDataUltimaEsterilizacao() { return dataUltimaEsterilizacao; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public int getPrazoValidadeDias() { return prazoValidadeDias; }
    public String getResponsavelEsterilizacao() { return responsavelEsterilizacao; }

    public void setStatus(StatusEsterilizacao status) {
        validarAtivo();
        this.status = status;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        validarAtivo();
        this.dataVencimento = dataVencimento;
    }

    public void setDataUltimaEsterilizacao(LocalDate data) {
        validarAtivo();
        this.dataUltimaEsterilizacao = data;
    }
}
