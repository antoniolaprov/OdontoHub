package com.g4.odontohub.pagamento.infrastructure.config;

import com.g4.odontohub.pagamento.application.PagamentoApplicationService;
import com.g4.odontohub.pagamento.domain.repository.PagamentoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root do contexto de Pagamento: liga a camada de aplicação
 * ao adapter JPA (camada de infraestrutura) como bean Spring.
 */
@Configuration
public class PagamentoBeanConfig {

    @Bean
    public PagamentoApplicationService pagamentoApplicationService(PagamentoRepository pagamentoRepository) {
        return new PagamentoApplicationService(pagamentoRepository);
    }
}
