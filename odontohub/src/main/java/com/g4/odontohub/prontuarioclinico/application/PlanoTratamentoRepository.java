package com.g4.odontohub.prontuarioclinico.application;

import com.g4.odontohub.prontuarioclinico.domain.model.PlanoTratamento;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.PlanoId;

public interface PlanoTratamentoRepository {
    void salvar(PlanoTratamento plano);
    PlanoTratamento buscarPorId(PlanoId id);
    PlanoTratamento buscarPorPacienteId(Long pacienteId);
    void remover(PlanoId id);
}