package com.g4.odontohub.prontuarioclinico.domain.service;

public class PlanoTratamentoService {
    private final AnamneseService anamneseService;

    public PlanoTratamentoService(AnamneseService anamneseService) {
        this.anamneseService = anamneseService;
    }

    public void criarPlano(Long pacienteId, Long dentistaId) {
        if (!anamneseService.anamneseExiste(pacienteId)) {
            throw new IllegalStateException("Paciente não possui anamnese registrada");
        }
    }
}