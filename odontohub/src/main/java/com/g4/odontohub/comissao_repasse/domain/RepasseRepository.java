package com.g4.odontohub.comissao_repasse.domain;

import java.util.Optional;

public interface RepasseRepository {
    void salvar(Repasse repasse);
    Optional<Repasse> buscarPorEspecialista(String nomeEspecialista);
}