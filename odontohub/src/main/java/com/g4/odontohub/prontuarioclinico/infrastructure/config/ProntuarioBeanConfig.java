package com.g4.odontohub.prontuarioclinico.infrastructure.config;

import com.g4.odontohub.prontuarioclinico.application.AnamneseRepository;
import com.g4.odontohub.prontuarioclinico.application.PlanoTratamentoRepository;
import com.g4.odontohub.prontuarioclinico.application.ProntuarioApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProntuarioBeanConfig {

    /** Anamnese (F02) e Plano de Tratamento (F03) persistem em JPA/H2. */
    @Bean
    public ProntuarioApplicationService prontuarioApplicationService(
            AnamneseRepository anamneseRepository, PlanoTratamentoRepository planoRepository) {
        return new ProntuarioApplicationService(anamneseRepository, planoRepository);
    }
}
