package com.g4.odontohub.inadimplencia.domain.repository;

import com.g4.odontohub.inadimplencia.domain.model.TentativaCobranca;

import java.util.List;

public interface TentativaCobrancaRepository {
    void salvar(TentativaCobranca tentativa);
    List<TentativaCobranca> listarPorPaciente(String pacienteId);
}
