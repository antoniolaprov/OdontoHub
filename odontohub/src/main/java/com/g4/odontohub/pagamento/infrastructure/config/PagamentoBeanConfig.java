package com.g4.odontohub.pagamento.infrastructure.config;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.pagamento.application.PagamentoApplicationService;
import com.g4.odontohub.pagamento.domain.repository.PagamentoRepository;
import org.springframework.boot.CommandLineRunner;
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

    /**
     * Semeia parcelas de exemplo para a tela de Pagamentos não iniciar vazia
     * (só se ainda não houver nenhuma). Deixa uma parcela aguardando comprovante
     * para exibir um item pendente no histórico — sem gerar entrada no fluxo de
     * caixa (a entrada só é gerada ao confirmar/registrar pagamento).
     */
    @Bean
    public CommandLineRunner seedPagamentos(PagamentoApplicationService pagamentoService) {
        return args -> {
            if (!pagamentoService.parcelas().isEmpty()) {
                return;
            }
            pagamentoService.criarParcela("Maria Santos", 600.0);
            pagamentoService.criarParcela("João Pereira", 350.0);
            pagamentoService.criarParcela("Ana Costa", 200.0);
            pagamentoService.lancarAguardandoComprovante("Ana Costa", "PIX");
        };
    }
}
