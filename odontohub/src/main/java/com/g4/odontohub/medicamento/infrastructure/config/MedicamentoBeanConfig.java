package com.g4.odontohub.medicamento.infrastructure.config;

import com.g4.odontohub.medicamento.application.MedicamentoApplicationService;
import com.g4.odontohub.medicamento.domain.repository.MedicamentoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MedicamentoBeanConfig {

    @Bean
    public MedicamentoApplicationService medicamentoApplicationService(MedicamentoRepository repositorio) {
        return new MedicamentoApplicationService(repositorio);
    }
}
