package com.g4.odontohub.inadimplencia.domain;

import java.util.Optional;

/**
 * Porta de repositório para RegistroInadimplencia.
 */
public interface InadimplenciaRepository {
    void salvar(RegistroInadimplencia registro);
    Optional<RegistroInadimplencia> buscarPorPaciente(String nomePaciente);
}
