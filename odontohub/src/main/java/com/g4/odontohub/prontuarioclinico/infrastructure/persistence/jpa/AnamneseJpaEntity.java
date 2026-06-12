package com.g4.odontohub.prontuarioclinico.infrastructure.persistence.jpa;

import com.g4.odontohub.prontuarioclinico.domain.model.Anamnese;
import com.g4.odontohub.prontuarioclinico.domain.model.AnamneseId;
import com.g4.odontohub.prontuarioclinico.domain.model.Anamnesepaciente;
import com.g4.odontohub.prontuarioclinico.domain.model.VersaoAnamnese;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "anamnese")
public class AnamneseJpaEntity {

    @Id
    private Long pacienteId;
    private Long anamneseId;
    @Column(length = 1000)
    private String alergias;
    @Column(length = 1000)
    private String contraindicacoes;
    @Column(length = 1000)
    private String condicoesSistemicas;
    private int versaoAtual;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataRegistro;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataUltimaAtualizacao;
    private String responsavelCadastro;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "anamnese_versao", joinColumns = @JoinColumn(name = "paciente_id"))
    private List<VersaoAnamneseEmbeddable> historico = new ArrayList<>();

    protected AnamneseJpaEntity() {
    }

    public static AnamneseJpaEntity fromDomain(Anamnese a) {
        AnamneseJpaEntity e = new AnamneseJpaEntity();
        e.pacienteId = a.getPacienteId().pacienteId();
        e.anamneseId = a.getId() == null ? null : a.getId().id();
        e.alergias = a.getAlergias();
        e.contraindicacoes = a.getContraindicacoes();
        e.condicoesSistemicas = a.getCondicoesSistemicas();
        e.versaoAtual = a.getVersaoAtual();
        e.dataRegistro = a.getDataRegistro();
        e.dataUltimaAtualizacao = a.getDataUltimaAtualizacao();
        e.responsavelCadastro = a.getResponsavelCadastro();
        e.historico = a.getHistoricoVersoes().stream()
                .map(VersaoAnamneseEmbeddable::fromDomain)
                .collect(Collectors.toList());
        return e;
    }

    public Anamnese toDomain() {
        List<VersaoAnamnese> hist = historico.stream()
                .map(VersaoAnamneseEmbeddable::toDomain)
                .collect(Collectors.toList());
        return Anamnese.reconstituir(new AnamneseId(anamneseId), new Anamnesepaciente(pacienteId),
                alergias, contraindicacoes, condicoesSistemicas, versaoAtual, dataRegistro,
                dataUltimaAtualizacao, responsavelCadastro, hist);
    }
}
