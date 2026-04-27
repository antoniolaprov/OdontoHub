package com.g4.odontohub.comissao_repasse.domain;

import java.util.Optional;

public interface RegraComissaoRepository {
    void salvar(RegraComissao regra);
    Optional<RegraComissao> buscarPorEspecialistaEProcedimento(String nomeEspecialista, String nomeProcedimento);
}