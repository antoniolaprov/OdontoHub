package com.g4.odontohub.prontuarioclinico.domain.model;

import java.util.Date;

public record VersaoAnamnese(
    int versao, 
    String alergias, 
    String contraindicacoes, 
    String condicoesSistemicas, 
    Date dataAlteracao, 
    String responsavel
) {}