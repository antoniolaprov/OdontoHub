package com.g4.odontohub.infra;

import com.g4.odontohub.comissao_repasse.domain.Comissao;
import com.g4.odontohub.comissao_repasse.domain.ComissaoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryComissaoRepository implements ComissaoRepository {

    private final List<Comissao> banco = new ArrayList<>();

    @Override
    public void salvar(Comissao comissao) {
        banco.removeIf(c -> c.getId().equals(comissao.getId()));
        banco.add(comissao);
    }

    @Override
    public Optional<Comissao> buscarPorEspecialistaEProcedimento(String nomeEspecialista, String nomeProcedimento) {
        return banco.stream()
                .filter(c -> c.getNomeEspecialista().equals(nomeEspecialista)
                        && c.getNomeProcedimento().equals(nomeProcedimento))
                .findFirst();
    }

    @Override
    public Optional<Comissao> buscarPorEspecialista(String nomeEspecialista) {
        return banco.stream()
                .filter(c -> c.getNomeEspecialista().equals(nomeEspecialista))
                .findFirst();
    }
}