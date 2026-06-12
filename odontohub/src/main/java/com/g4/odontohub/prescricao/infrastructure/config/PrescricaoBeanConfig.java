package com.g4.odontohub.prescricao.infrastructure.config;

import com.g4.odontohub.prescricao.application.PrescricaoApplicationService;
import com.g4.odontohub.prescricao.domain.repository.PrescricaoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrescricaoBeanConfig {

    @Bean
    public PrescricaoApplicationService prescricaoApplicationService(PrescricaoRepository repositorio) {
        return new PrescricaoApplicationService(repositorio);
    }
}
