package com.g4.odontohub.inadimplencia.domain.repository;

import com.g4.odontohub.inadimplencia.domain.model.Acordo;
import com.g4.odontohub.inadimplencia.domain.model.StatusAcordo;

import java.util.List;
import java.util.Optional;

public interface AcordoRepository {
    void salvar(Acordo acordo);
    Optional<Acordo> buscarPorId(String id);
    List<Acordo> listarPorPaciente(String pacienteId);
    Optional<Acordo> buscarAtivoPorPaciente(String pacienteId);
    List<Acordo> listarPorStatus(StatusAcordo status);
}
