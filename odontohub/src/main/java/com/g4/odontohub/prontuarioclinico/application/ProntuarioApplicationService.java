package com.g4.odontohub.prontuarioclinico.application;

import com.g4.odontohub.prontuarioclinico.domain.service.AnamneseService;
import com.g4.odontohub.prontuarioclinico.domain.service.PlanoTratamentoService;

public class ProntuarioApplicationService {
    
    private final AnamneseService anamneseService;
    private final PlanoTratamentoService planoTratamentoService;
    private final AnamneseRepository anamneseRepository;
    private final PlanoTratamentoRepository planoRepository;

    public ProntuarioApplicationService() {
        this.anamneseRepository = new InMemoryAnamneseRepository();
        this.planoRepository = new InMemoryPlanoTratamentoRepository();
        
        this.anamneseService = new AnamneseService(this.anamneseRepository);
        this.planoTratamentoService = new PlanoTratamentoService(this.anamneseService, this.planoRepository);
    }

    public AnamneseService getAnamneseService() { return anamneseService; }
    public PlanoTratamentoService getPlanoTratamentoService() { return planoTratamentoService; }
    public AnamneseRepository getAnamneseRepository() { return anamneseRepository; }
    public PlanoTratamentoRepository getPlanoRepository() { return planoRepository; }
}