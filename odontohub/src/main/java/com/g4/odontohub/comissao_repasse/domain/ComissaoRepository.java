package com.g4.odontohub.comissao_repasse.domain;

import java.util.Optional;

public interface ComissaoRepository {
    void salvar(Comissao comissao);
    Optional<Comissao> buscarPorEspecialistaEProcedimento(String nomeEspecialista, String nomeProcedimento);
    Optional<Comissao> buscarPorEspecialista(String nomeEspecialista);
}