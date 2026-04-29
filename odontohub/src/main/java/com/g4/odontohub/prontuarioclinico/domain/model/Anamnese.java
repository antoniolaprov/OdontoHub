package com.g4.odontohub.prontuarioclinico.domain.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Anamnese {
    private AnamneseId id;
    private Anamnesepaciente pacienteId;
    private String alergias;
    private String contraindicacoes;
    private String condicoesSistemicas;
    private int versaoAtual;
    private Date dataRegistro;
    private Date dataUltimaAtualizacao;
    private String responsavelCadastro;
    
    private final List<VersaoAnamnese> historicoVersoes = new ArrayList<>();

    // MUDANÇA AQUI: Adicionado 'condicoesSistemicas' no construtor
    public Anamnese(AnamneseId id, Anamnesepaciente pacienteId, String alergias, String contraindicacoes, String condicoesSistemicas, String responsavelCadastro) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.alergias = alergias;
        this.contraindicacoes = contraindicacoes;
        this.condicoesSistemicas = condicoesSistemicas; // Setado direto na criação
        this.versaoAtual = 1;
        this.dataRegistro = new Date();
        this.dataUltimaAtualizacao = this.dataRegistro;
        this.responsavelCadastro = responsavelCadastro;
    }

    public void atualizarCondicoesSistemicas(String condicoes, String responsavel) {
        salvarVersaoAtualNoHistorico();
        this.condicoesSistemicas = condicoes;
        atualizarMetadados(responsavel);
    }

    public void adicionarAlergia(String novaAlergia, String responsavel) {
        salvarVersaoAtualNoHistorico();
        if (this.alergias == null || this.alergias.isEmpty()) {
            this.alergias = novaAlergia;
        } else {
            this.alergias += ", " + novaAlergia;
        }
        atualizarMetadados(responsavel);
    }

    private void salvarVersaoAtualNoHistorico() {
        this.historicoVersoes.add(new VersaoAnamnese(
            this.versaoAtual, 
            this.alergias, 
            this.contraindicacoes, 
            this.condicoesSistemicas, 
            this.dataUltimaAtualizacao, 
            this.responsavelCadastro
        ));
    }

    private void atualizarMetadados(String responsavel) {
        this.versaoAtual++;
        this.dataUltimaAtualizacao = new Date();
        this.responsavelCadastro = responsavel;
    }

    public AnamneseId getId() { return id; }
    public Anamnesepaciente getPacienteId() { return pacienteId; }
    public String getAlergias() { return alergias; }
    public String getContraindicacoes() { return contraindicacoes; }
    public String getCondicoesSistemicas() { return condicoesSistemicas; }
    public int getVersaoAtual() { return versaoAtual; }
    public Date getDataRegistro() { return dataRegistro; }
    public Date getDataUltimaAtualizacao() { return dataUltimaAtualizacao; }
    public String getResponsavelCadastro() { return responsavelCadastro; }
    public List<VersaoAnamnese> getHistoricoVersoes() { return historicoVersoes; }
}