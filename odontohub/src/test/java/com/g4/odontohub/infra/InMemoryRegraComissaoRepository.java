package com.g4.odontohub.infra;

import com.g4.odontohub.comissao_repasse.domain.RegraComissao;
import com.g4.odontohub.comissao_repasse.domain.RegraComissaoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryRegraComissaoRepository implements RegraComissaoRepository {

    private final List<RegraComissao> banco = new ArrayList<>();

    @Override
    public void salvar(RegraComissao regra) {
        banco.removeIf(r -> r.getNomeEspecialista().equals(regra.getNomeEspecialista())
                && r.getNomeProcedimento().equals(regra.getNomeProcedimento()));
        banco.add(regra);
    }

    @Override
    public Optional<RegraComissao> buscarPorEspecialistaEProcedimento(String nomeEspecialista, String nomeProcedimento) {
        return banco.stream()
                .filter(r -> r.getNomeEspecialista().equals(nomeEspecialista)
                        && r.getNomeProcedimento().equals(nomeProcedimento))
                .findFirst();
    }
}