package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.config;

import com.g4.odontohub.relacionamentopaciente.recall.application.RecallApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecallBeanConfig {

    @Bean
    public RecallApplicationService recallApplicationService(RecallRepository repositorio) {
        return new RecallApplicationService(repositorio);
    }
}
