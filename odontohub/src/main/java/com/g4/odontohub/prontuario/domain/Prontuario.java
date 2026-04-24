package com.g4.odontohub.prontuario.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Prontuario {

    private Long id;
    private Long pacienteId;
    private StatusProntuario status;
    private String justificativaEncerramento;
    private List<FichaClinica> fichas;

    private Prontuario() {}

    public static Prontuario criar(Long id, Long pacienteId) {
        Prontuario p = new Prontuario();
        p.id = id;
        p.pacienteId = pacienteId;
        p.status = StatusProntuario.ATIVO;
        p.fichas = new ArrayList<>();
        return p;
    }

    public void adicionarFicha(Long fichaId, String evolucao) {
        this.fichas.add(FichaClinica.criar(fichaId, evolucao));
    }

    public void confirmarFicha(Long fichaId) {
        buscarFichaPorId(fichaId).confirmar();
    }

    public void editarEvolucaoFicha(Long fichaId, String novaEvolucao) {
        buscarFichaPorId(fichaId).editarEvolucao(novaEvolucao);
    }

    public void encerrar(String justificativa) {
        this.status = StatusProntuario.ENCERRADO;
        this.justificativaEncerramento = justificativa;
    }

    public List<FichaClinica> getFichas() {
        return Collections.unmodifiableList(fichas);
    }

    private FichaClinica buscarFichaPorId(Long fichaId) {
        return fichas.stream()
                .filter(f -> f.getId().equals(fichaId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Ficha clínica não encontrada: " + fichaId));
    }
}
