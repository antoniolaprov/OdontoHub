package com.g4.odontohub.estoque.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private String tipo;
    private final List<String> codigosComponentes = new ArrayList<>();

    public Instrumento(InstrumentoId id, String nome, String categoria,
                       String codigoIdentificador, int prazoValidadeDias) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.codigoIdentificador = codigoIdentificador;
        this.prazoValidadeDias = prazoValidadeDias;
        this.statusInstrumento = StatusInstrumento.ATIVO;
        this.status = StatusEsterilizacao.CONTAMINADO;
        this.tipo = "INDIVIDUAL";
    }

    /** Reconstitui o agregado a partir da persistência (camada de infraestrutura). */
    public static Instrumento reconstituir(InstrumentoId id, String nome, String categoria, String codigoIdentificador,
                                           StatusInstrumento statusInstrumento, StatusEsterilizacao status,
                                           LocalDate dataUltimaEsterilizacao, LocalDate dataVencimento,
                                           int prazoValidadeDias, String responsavelEsterilizacao,
                                           String tipo, List<String> codigosComponentes) {
        Instrumento i = new Instrumento(id, nome, categoria, codigoIdentificador, prazoValidadeDias);
        i.statusInstrumento = statusInstrumento;
        i.status = status;
        i.dataUltimaEsterilizacao = dataUltimaEsterilizacao;
        i.dataVencimento = dataVencimento;
        i.responsavelEsterilizacao = responsavelEsterilizacao;
        i.tipo = tipo;
        if (codigosComponentes != null) {
            i.codigosComponentes.addAll(codigosComponentes);
        }
        return i;
    }

    public void desativar() {
        this.statusInstrumento = StatusInstrumento.INATIVO;
    }

    public void reativar() {
        this.statusInstrumento = StatusInstrumento.ATIVO;
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
    public String getTipo() { return tipo; }
    public List<String> getCodigosComponentes() { return Collections.unmodifiableList(codigosComponentes); }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void adicionarComponentes(List<String> codigosComponentes) {
        this.codigosComponentes.clear();
        this.codigosComponentes.addAll(codigosComponentes);
    }

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
