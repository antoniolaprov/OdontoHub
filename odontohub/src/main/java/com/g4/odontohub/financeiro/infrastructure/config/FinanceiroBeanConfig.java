package com.g4.odontohub.financeiro.infrastructure.config;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;
import com.g4.odontohub.financeiro.domain.repository.LancamentoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinanceiroBeanConfig {

    @Bean
    public FluxoCaixaApplicationService fluxoCaixaApplicationService(LancamentoRepository lancamentoRepository) {
        return new FluxoCaixaApplicationService(lancamentoRepository);
    }

    @Bean
    public InadimplenciaApplicationService inadimplenciaApplicationService(InadimplenciaRepository inadimplenciaRepository) {
        return new InadimplenciaApplicationService(inadimplenciaRepository);
    }
}
