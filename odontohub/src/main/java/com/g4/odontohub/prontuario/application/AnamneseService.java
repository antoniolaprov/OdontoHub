package com.g4.odontohub.prontuario.application;

import com.g4.odontohub.prontuario.domain.Anamnese;
import com.g4.odontohub.prontuario.domain.AnamneseRepository;
import com.g4.odontohub.shared.exception.DomainException;

import java.util.List;

public class AnamneseService {

    private final AnamneseRepository repository;

    public AnamneseService(AnamneseRepository repository) {
        this.repository = repository;
    }

    public Anamnese registrar(Long pacienteId,
                               List<String> alergias,
                               List<String> contraindicacoes,
                               String responsavel) {
        Anamnese anamnese = Anamnese.criar(pacienteId, alergias, contraindicacoes, responsavel);
        return repository.salvar(anamnese);
    }

    // Regra cross-context: plano de tratamento exige anamnese prévia
    public void validarAnamneseParaPlano(Long pacienteId) {
        repository.buscarPorPacienteId(pacienteId)
                .orElseThrow(() -> new DomainException(
                        "É necessário registrar a anamnese antes de criar um plano de tratamento"));
    }

    public void verificarAlergiaParaProcedimento(Long pacienteId, String substanciaUtilizada) {
        Anamnese anamnese = buscarOuFalhar(pacienteId);
        anamnese.verificarAlergiaParaSubstancia(substanciaUtilizada).ifPresent(alergia -> {
            throw new DomainException("Paciente alérgico a substância relacionada: " + alergia);
        });
    }

    public Anamnese adicionarAlergia(Long pacienteId, String novaAlergia, String responsavel) {
        Anamnese anamnese = buscarOuFalhar(pacienteId);
        anamnese.adicionarAlergia(novaAlergia, responsavel);
        return repository.salvar(anamnese);
    }

    public Anamnese atualizarCondicoesSistemicas(Long pacienteId, String condicoes, String responsavel) {
        Anamnese anamnese = buscarOuFalhar(pacienteId);
        anamnese.atualizarCondicoesSistemicas(condicoes, responsavel);
        return repository.salvar(anamnese);
    }

    private Anamnese buscarOuFalhar(Long pacienteId) {
        return repository.buscarPorPacienteId(pacienteId)
                .orElseThrow(() -> new DomainException("Anamnese não encontrada para o paciente: " + pacienteId));
    }
}
