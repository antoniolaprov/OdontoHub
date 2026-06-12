package com.g4.odontohub.cadastropaciente.infrastructure.config;

import com.g4.odontohub.cadastropaciente.application.PacienteApplicationService;
import com.g4.odontohub.cadastropaciente.domain.repository.PacienteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CadastroPacienteBeanConfig {

    @Bean
    public PacienteApplicationService pacienteApplicationService(PacienteRepository repositorio) {
        return new PacienteApplicationService(repositorio);
    }
}
