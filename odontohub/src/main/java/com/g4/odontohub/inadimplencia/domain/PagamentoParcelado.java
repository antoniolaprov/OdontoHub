package com.g4.odontohub.inadimplencia.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root: Pagamento Parcelado de um paciente.
 * Agrega as parcelas e controla o estado de inadimplência.
 */
@Getter
public class PagamentoParcelado {

    private Long id;
    private String nomePaciente;
    private List<ParcelaInadimplencia> parcelas;

    private PagamentoParcelado() {}

    public static PagamentoParcelado criar(Long id, String nomePaciente) {
        PagamentoParcelado pp = new PagamentoParcelado();
        pp.id = id;
        pp.nomePaciente = nomePaciente;
        pp.parcelas = new ArrayList<>();
        return pp;
    }

    public void adicionarParcela(ParcelaInadimplencia parcela) {
        this.parcelas.add(parcela);
    }

    public ParcelaInadimplencia getPorNumero(int numero) {
        return parcelas.stream()
                .filter(p -> p.getNumero() == numero)
                .findFirst()
                .orElseThrow(() -> new com.g4.odontohub.shared.exception.DomainException(
                        "Parcela " + numero + " não encontrada"));
    }

    public boolean possuiInadimplencia() {
        return parcelas.stream().anyMatch(ParcelaInadimplencia::estaInadimplente);
    }

    public List<ParcelaInadimplencia> getParcelas() {
        return Collections.unmodifiableList(parcelas);
    }
}
