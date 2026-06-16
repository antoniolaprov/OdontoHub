package com.g4.odontohub.pagamento.infrastructure.config;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.pagamento.application.PagamentoApplicationService;
import com.g4.odontohub.pagamento.domain.repository.PagamentoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root do contexto de Pagamento: liga a camada de aplicação
 * ao adapter JPA (camada de infraestrutura) como bean Spring e integra o
 * recebimento de parcelas com o fluxo de caixa do contexto Financeiro.
 */
@Configuration
public class PagamentoBeanConfig {

    @Bean
    public PagamentoApplicationService pagamentoApplicationService(
            PagamentoRepository pagamentoRepository, FluxoCaixaApplicationService fluxoCaixaService) {
        return new PagamentoApplicationService(pagamentoRepository, fluxoCaixaService);
    }
}
