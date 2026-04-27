package com.g4.odontohub.infra;

import com.g4.odontohub.comissao_repasse.domain.Repasse;
import com.g4.odontohub.comissao_repasse.domain.RepasseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryRepasseRepository implements RepasseRepository {

    private final List<Repasse> banco = new ArrayList<>();

    @Override
    public void salvar(Repasse repasse) {
        banco.removeIf(r -> r.getId().equals(repasse.getId()));
        banco.add(repasse);
    }

    @Override
    public Optional<Repasse> buscarPorEspecialista(String nomeEspecialista) {
        return banco.stream()
                .filter(r -> r.getNomeEspecialista().equals(nomeEspecialista))
                .findFirst();
    }
}