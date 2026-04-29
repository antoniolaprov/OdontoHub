package com.g4.odontohub.estoque.domain.model;

import java.time.LocalDate;

public class Instrumento {

    private final InstrumentoId id;
    private final String nome;
    private final String categoria;
    private StatusEsterilizacao status;
    private LocalDate dataUltimaEsterilizacao;
    private LocalDate dataVencimento;
    private int prazoValidadeDias;
    private String responsavelEsterilizacao;

    public Instrumento(InstrumentoId id, String nome, int prazoValidadeDias) {
        this.id = id;
        this.nome = nome;
        this.categoria = "";
        this.prazoValidadeDias = prazoValidadeDias;
        this.status = StatusEsterilizacao.CONTAMINADO;
    }

    public void marcarComoEsteril(LocalDate dataEsterilizacao, String responsavel) {
        this.status = StatusEsterilizacao.ESTERIL;
        this.dataUltimaEsterilizacao = dataEsterilizacao;
        this.responsavelEsterilizacao = responsavel;
        this.dataVencimento = dataEsterilizacao.plusDays(prazoValidadeDias);
    }

    public void marcarComoContaminado() {
        this.status = StatusEsterilizacao.CONTAMINADO;
    }

    public void marcarComoVencido() {
        this.status = StatusEsterilizacao.VENCIDO;
    }

    public void recalcularVencimento(int novoPrazoDias) {
        this.prazoValidadeDias = novoPrazoDias;
        if (this.dataUltimaEsterilizacao != null) {
            this.dataVencimento = this.dataUltimaEsterilizacao.plusDays(novoPrazoDias);
        }
    }

    public InstrumentoId getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public StatusEsterilizacao getStatus() { return status; }
    public LocalDate getDataUltimaEsterilizacao() { return dataUltimaEsterilizacao; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public int getPrazoValidadeDias() { return prazoValidadeDias; }
    public String getResponsavelEsterilizacao() { return responsavelEsterilizacao; }

    public void setStatus(StatusEsterilizacao status) { this.status = status; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public void setDataUltimaEsterilizacao(LocalDate data) { this.dataUltimaEsterilizacao = data; }
}