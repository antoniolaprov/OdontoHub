package com.g4.odontohub.financeiro.domain.event;

import java.time.LocalDate;

public record AlertaFluxoNegativo(double saldoProjetado, LocalDate mesReferencia) {}
