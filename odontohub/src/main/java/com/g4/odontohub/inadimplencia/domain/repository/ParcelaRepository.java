package com.g4.odontohub.inadimplencia.domain.repository;

import com.g4.odontohub.inadimplencia.domain.model.Parcela;
import com.g4.odontohub.inadimplencia.domain.model.StatusParcela;

import java.util.List;
import java.util.Optional;

public interface ParcelaRepository {
    void salvar(Parcela parcela);
    Optional<Parcela> buscarPorId(String id);
    List<Parcela> listarPorPaciente(String pacienteId);
    List<Parcela> listarPorPacienteEStatus(String pacienteId, StatusParcela status);
    List<Parcela> listarVencidas();
}
