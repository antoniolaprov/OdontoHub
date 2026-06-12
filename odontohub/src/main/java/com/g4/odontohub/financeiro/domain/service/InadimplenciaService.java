package com.g4.odontohub.financeiro.domain.service;

import com.g4.odontohub.financeiro.domain.model.Parcela;
import com.g4.odontohub.financeiro.domain.model.StatusParcela;

import java.util.List;

public class InadimplenciaService {

    public boolean pacienteRestrito(List<Parcela> parcelas) {
        return parcelas.stream()
                .filter(p -> p.getStatus() == StatusParcela.VENCIDA || p.getStatus() == StatusParcela.PENDENTE)
                .anyMatch(Parcela::deveRestringirPaciente);
    }

    public boolean pacienteInadimplente(List<Parcela> parcelas) {
        return parcelas.stream()
                .anyMatch(p -> p.calcularDiasAtraso() > 0);
    }

    public void calcularJurosEMultasDasParcelas(List<Parcela> parcelas) {
        parcelas.forEach(Parcela::calcularJurosEMulta);
    }
}