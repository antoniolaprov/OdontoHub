package com.g4.odontohub.comissao_repasse.domain;

import java.util.List;

public interface LancamentoCaixaRepository {
    void salvar(LancamentoCaixa lancamento);
    List<LancamentoCaixa> listarTodos();
}