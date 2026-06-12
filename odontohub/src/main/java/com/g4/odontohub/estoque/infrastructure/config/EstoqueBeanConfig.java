package com.g4.odontohub.estoque.infrastructure.config;

import com.g4.odontohub.estoque.application.InstrumentoApplicationService;
import com.g4.odontohub.estoque.application.MaterialApplicationService;
import com.g4.odontohub.estoque.domain.repository.InstrumentoRepository;
import com.g4.odontohub.estoque.domain.repository.MaterialRepository;
import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EstoqueBeanConfig {

    @Bean
    public MaterialApplicationService materialApplicationService(
            FluxoCaixaApplicationService fluxoCaixaService, MaterialRepository materialRepository) {
        return new MaterialApplicationService(fluxoCaixaService, materialRepository);
    }

    @Bean
    public InstrumentoApplicationService instrumentoApplicationService(InstrumentoRepository instrumentoRepository) {
        return new InstrumentoApplicationService(instrumentoRepository);
    }
}
