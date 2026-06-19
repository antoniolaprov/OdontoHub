package com.g4.odontohub.clinica.infrastructure.persistence;

import com.g4.odontohub.clinica.domain.model.Clinica;
import com.g4.odontohub.clinica.domain.model.ClinicaId;
import com.g4.odontohub.clinica.domain.repository.ClinicaRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryClinicaRepository implements ClinicaRepository {

    private final Map<Long, Clinica> clinicas = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(Clinica clinica) {
        clinicas.put(clinica.getId().id(), clinica);
    }

    @Override
    public Clinica buscarPorId(ClinicaId id) {
        return clinicas.get(id.id());
    }

    @Override
    public Clinica buscarPorEmail(String email) {
        if (email == null) return null;
        return clinicas.values().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Clinica buscarPorCnpj(String cnpj) {
        if (cnpj == null) return null;
        return clinicas.values().stream()
                .filter(c -> c.getCnpj().equals(cnpj))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Clinica> todas() {
        return new ArrayList<>(clinicas.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
