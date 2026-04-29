package com.g4.odontohub.prontuarioclinico.application;

import com.g4.odontohub.prontuarioclinico.domain.service.AnamneseService;
import com.g4.odontohub.prontuarioclinico.domain.service.PlanoTratamentoService;

public class ProntuarioApplicationService {
    
    private final AnamneseService anamneseService;
    private final PlanoTratamentoService planoTratamentoService;
    private final AnamneseRepository repository;

    public ProntuarioApplicationService() {
        this.repository = new InMemoryAnamneseRepository();
        this.anamneseService = new AnamneseService(this.repository);
        this.planoTratamentoService = new PlanoTratamentoService(this.anamneseService);
    }

    public AnamneseService getAnamneseService() { return anamneseService; }
    public PlanoTratamentoService getPlanoTratamentoService() { return planoTratamentoService; }
    public AnamneseRepository getRepository() { return repository; }
}