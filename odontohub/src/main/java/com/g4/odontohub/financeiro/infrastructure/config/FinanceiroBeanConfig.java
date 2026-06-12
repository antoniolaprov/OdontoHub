package com.g4.odontohub.financeiro.infrastructure.config;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinanceiroBeanConfig {

    @Bean
    public FluxoCaixaApplicationService fluxoCaixaApplicationService() {
        return new FluxoCaixaApplicationService();
    }

    @Bean
    public InadimplenciaApplicationService inadimplenciaApplicationService() {
        return new InadimplenciaApplicationService();
    }
}
