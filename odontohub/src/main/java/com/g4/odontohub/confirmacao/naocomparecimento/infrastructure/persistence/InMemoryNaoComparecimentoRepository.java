package com.g4.odontohub.confirmacao.naocomparecimento.infrastructure.persistence;

import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimento;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.repository.NaoComparecimentoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Adapter de persistência em memória (usado pelos testes BDD). */
public class InMemoryNaoComparecimentoRepository implements NaoComparecimentoRepository {

    private final Map<Long, NaoComparecimento> registros = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public void salvar(NaoComparecimento registro) {
        registros.put(registro.getId().id(), registro);
    }

    @Override
    public NaoComparecimento buscarPorAgendamento(Long agendamentoId) {
        return registros.values().stream()
                .filter(r -> r.getAgendamentoId().equals(agendamentoId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<NaoComparecimento> listarPorPaciente(String paciente) {
        return registros.values().stream()
                .filter(r -> r.getPaciente().equals(paciente))
                .collect(Collectors.toList());
    }

    @Override
    public List<NaoComparecimento> todos() {
        return new ArrayList<>(registros.values());
    }

    @Override
    public long proximoId() {
        return ++sequencia;
    }
}
