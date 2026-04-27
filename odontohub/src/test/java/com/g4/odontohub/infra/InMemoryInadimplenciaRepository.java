package com.g4.odontohub.infra;

import com.g4.odontohub.inadimplencia.domain.InadimplenciaRepository;
import com.g4.odontohub.inadimplencia.domain.RegistroInadimplencia;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação in-memory de InadimplenciaRepository para uso nos testes BDD.
 */
public class InMemoryInadimplenciaRepository implements InadimplenciaRepository {

    private final Map<String, RegistroInadimplencia> banco = new HashMap<>();

    @Override
    public void salvar(RegistroInadimplencia registro) {
        banco.put(registro.getNomePaciente(), registro);
    }

    @Override
    public Optional<RegistroInadimplencia> buscarPorPaciente(String nomePaciente) {
        return Optional.ofNullable(banco.get(nomePaciente));
    }
}
