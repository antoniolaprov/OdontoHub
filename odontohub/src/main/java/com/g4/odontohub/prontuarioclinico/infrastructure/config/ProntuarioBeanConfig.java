package com.g4.odontohub.prontuarioclinico.infrastructure.config;

import com.g4.odontohub.prontuarioclinico.application.ProntuarioApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProntuarioBeanConfig {

    @Bean
    public ProntuarioApplicationService prontuarioApplicationService() {
        return new ProntuarioApplicationService();
    }
}
