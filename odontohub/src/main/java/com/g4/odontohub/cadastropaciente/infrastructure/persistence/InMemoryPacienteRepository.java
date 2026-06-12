package com.g4.odontohub.cadastropaciente.infrastructure.persistence;

import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;
import com.g4.odontohub.cadastropaciente.domain.repository.PacienteRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryPacienteRepository implements PacienteRepository {

    private final Map<Long, Paciente> pacientes = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Paciente paciente) {
        pacientes.put(paciente.getId().id(), paciente);
    }

    @Override
    public Paciente buscarPorId(PacienteRegistroId id) {
        return pacientes.get(id.id());
    }

    @Override
    public Paciente buscarPorNome(String nomeCompleto) {
        return pacientes.values().stream()
                .filter(p -> p.getNomeCompleto().equals(nomeCompleto))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Paciente> todos() {
        return new ArrayList<>(pacientes.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
