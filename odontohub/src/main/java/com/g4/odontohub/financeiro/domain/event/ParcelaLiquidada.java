package com.g4.odontohub.financeiro.domain.event;

import com.g4.odontohub.financeiro.domain.model.ParcelaId;
import java.time.LocalDate;

public record ParcelaLiquidada(ParcelaId parcelaId, double valorPago, LocalDate dataPagamento) {}
