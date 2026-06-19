package com.g4.odontohub.clinica.domain.repository;

import com.g4.odontohub.clinica.domain.model.Clinica;
import com.g4.odontohub.clinica.domain.model.ClinicaId;

import java.util.List;

/** Porta de persistência do contexto de Clínica (camada de domínio). */
public interface ClinicaRepository {

    void salvar(Clinica clinica);

    Clinica buscarPorId(ClinicaId id);

    Clinica buscarPorEmail(String email);

    Clinica buscarPorCnpj(String cnpj);

    List<Clinica> todas();

    long proximoId();
}
