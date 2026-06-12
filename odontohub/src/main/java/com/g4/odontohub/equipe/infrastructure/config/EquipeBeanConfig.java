package com.g4.odontohub.equipe.infrastructure.config;

import com.g4.odontohub.equipe.application.ColaboradorApplicationService;
import com.g4.odontohub.equipe.domain.repository.ColaboradorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EquipeBeanConfig {

    @Bean
    public ColaboradorApplicationService colaboradorApplicationService(ColaboradorRepository repositorio) {
        return new ColaboradorApplicationService(repositorio);
    }
}
