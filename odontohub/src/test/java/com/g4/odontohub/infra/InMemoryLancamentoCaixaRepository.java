package com.g4.odontohub.infra;

import com.g4.odontohub.comissao_repasse.domain.LancamentoCaixa;
import com.g4.odontohub.comissao_repasse.domain.LancamentoCaixaRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryLancamentoCaixaRepository implements LancamentoCaixaRepository {

    private final List<LancamentoCaixa> banco = new ArrayList<>();

    @Override
    public void salvar(LancamentoCaixa lancamento) {
        banco.add(lancamento);
    }

    @Override
    public List<LancamentoCaixa> listarTodos() {
        return new ArrayList<>(banco);
    }
}