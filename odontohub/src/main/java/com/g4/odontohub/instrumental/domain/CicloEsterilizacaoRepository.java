package com.g4.odontohub.instrumental.domain;

public interface CicloEsterilizacaoRepository {
    void salvar(CicloEsterilizacao ciclo);
    CicloEsterilizacao buscarUltimoPorInstrumento(Long instrumentoId);
}