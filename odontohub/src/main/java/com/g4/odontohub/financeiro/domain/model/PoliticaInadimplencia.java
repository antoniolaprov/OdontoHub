package com.g4.odontohub.financeiro.domain.model;

public record PoliticaInadimplencia(
        int diasParaRestricao,
        double percentualMultaMensal,
        double percentualJurosDia
) {}
