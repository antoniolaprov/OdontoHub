package com.g4.odontohub.financeiro.domain.repository;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;

import java.util.List;

/** Porta de persistência de inadimplência e acordos (camada de domínio). */
public interface InadimplenciaRepository {

    void salvarVencida(String paciente, ParcelaCobranca parcela);

    List<ParcelaCobranca> vencidasDe(String paciente);

    void salvarAcordo(String paciente, Acordo acordo);

    Acordo acordoDe(String paciente);

    long proximoAcordoId();
}
