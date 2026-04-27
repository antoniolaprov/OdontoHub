package com.g4.odontohub.prontuario.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
public final class VersaoAnamnese {

    private final int versao;
    private final List<String> alergias;
    private final List<String> contraindicacoes;
    private final String condicoesSistemicas;
    private final LocalDateTime dataAlteracao;
    private final String responsavel;

    VersaoAnamnese(int versao, List<String> alergias, List<String> contraindicacoes,
                   String condicoesSistemicas, LocalDateTime dataAlteracao, String responsavel) {
        this.versao = versao;
        this.alergias = Collections.unmodifiableList(alergias);
        this.contraindicacoes = Collections.unmodifiableList(contraindicacoes);
        this.condicoesSistemicas = condicoesSistemicas;
        this.dataAlteracao = dataAlteracao;
        this.responsavel = responsavel;
    }
}
