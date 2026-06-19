package com.g4.odontohub.financeiro.domain.repository;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ContatoCobranca;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;

import java.util.List;
import java.util.Set;

public interface InadimplenciaRepository {

    void registrarPaciente(String paciente);

    Set<String> pacientes();

    void salvarVencida(String paciente, ParcelaCobranca parcela);

    List<ParcelaCobranca> vencidasDe(String paciente);

    void removerVencidas(String paciente);

    void salvarAcordo(String paciente, Acordo acordo);

    Acordo acordoDe(String paciente);

    void salvarContato(String paciente, ContatoCobranca contato);

    List<ContatoCobranca> contatosDe(String paciente);

    long proximoAcordoId();
}
