package com.g4.odontohub.medicamento.domain.repository;

import com.g4.odontohub.medicamento.domain.model.Medicamento;

import java.util.List;

/** Porta de persistência do catálogo de medicamentos (camada de domínio). */
public interface MedicamentoRepository {

    void salvar(Medicamento medicamento);

    Medicamento buscarPorNome(String nomeComercial);

    List<Medicamento> todos();

    long proximoId();
}
