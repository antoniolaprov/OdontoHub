package com.g4.odontohub.financeiro.domain.event;

import com.g4.odontohub.financeiro.domain.model.LancamentoId;
import java.time.LocalDate;

public record EntradaGeradaAutomaticamente(LancamentoId lancamentoId, double valor, LocalDate data, String origem) {}
