package com.g4.odontohub.financeiro.infrastructure.persistence;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ContatoCobranca;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InMemoryInadimplenciaRepository implements InadimplenciaRepository {

    private final Set<String> pacientes = new HashSet<>();
    private final Map<String, List<ParcelaCobranca>> vencidas = new HashMap<>();
    private final Map<String, Acordo> acordos = new HashMap<>();
    private final Map<String, List<ContatoCobranca>> contatos = new HashMap<>();
    private long acordoSeq = 0;

    @Override
    public void registrarPaciente(String paciente) {
        pacientes.add(paciente);
    }

    @Override
    public Set<String> pacientes() {
        return new HashSet<>(pacientes);
    }

    @Override
    public void salvarVencida(String paciente, ParcelaCobranca parcela) {
        registrarPaciente(paciente);
        vencidas.computeIfAbsent(paciente, p -> new ArrayList<>()).add(parcela);
    }

    @Override
    public List<ParcelaCobranca> vencidasDe(String paciente) {
        return new ArrayList<>(vencidas.getOrDefault(paciente, new ArrayList<>()));
    }

    @Override
    public void removerVencidas(String paciente) {
        vencidas.remove(paciente);
    }

    @Override
    public void salvarAcordo(String paciente, Acordo acordo) {
        registrarPaciente(paciente);
        acordos.put(paciente, acordo);
    }

    @Override
    public Acordo acordoDe(String paciente) {
        return acordos.get(paciente);
    }

    @Override
    public void salvarContato(String paciente, ContatoCobranca contato) {
        registrarPaciente(paciente);
        contatos.computeIfAbsent(paciente, p -> new ArrayList<>()).add(contato);
    }

    @Override
    public List<ContatoCobranca> contatosDe(String paciente) {
        return new ArrayList<>(contatos.getOrDefault(paciente, new ArrayList<>()));
    }

    @Override
    public long proximoAcordoId() {
        return ++acordoSeq;
    }
}
