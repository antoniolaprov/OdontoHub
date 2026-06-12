package com.g4.odontohub.relacionamentopaciente.churn.infrastructure.config;

import com.g4.odontohub.relacionamentopaciente.churn.application.ChurnApplicationService;
import com.g4.odontohub.relacionamentopaciente.churn.domain.repository.ChurnRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChurnBeanConfig {

    @Bean
    public ChurnApplicationService churnApplicationService(ChurnRepository repositorio) {
        return new ChurnApplicationService(repositorio);
    }
}
