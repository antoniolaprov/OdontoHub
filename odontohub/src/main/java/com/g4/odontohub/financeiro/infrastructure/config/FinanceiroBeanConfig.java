package com.g4.odontohub.financeiro.infrastructure.config;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;
import com.g4.odontohub.financeiro.domain.repository.LancamentoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class FinanceiroBeanConfig {

    @Bean
    public FluxoCaixaApplicationService fluxoCaixaApplicationService(LancamentoRepository lancamentoRepository) {
        return new FluxoCaixaApplicationService(lancamentoRepository);
    }

    /** Semeia lançamentos de exemplo para o fluxo de caixa não iniciar vazio (só se não houver nenhum). */
    @Bean
    public CommandLineRunner seedFluxoCaixa(FluxoCaixaApplicationService fluxoCaixa) {
        return args -> {
            if (!fluxoCaixa.getLancamentos().isEmpty()) {
                return;
            }
            fluxoCaixa.registrarEntradaManual(1200.0, "Consulta - Maria Silva");
            fluxoCaixa.registrarEntradaManual(800.0, "Limpeza - João Pereira");
            fluxoCaixa.registrarSaidaManual(450.0, "Insumos", "Compra de luvas e máscaras");
            fluxoCaixa.registrarEntradaPrevista(2000.0, "Parcela tratamento - Ana Costa", LocalDate.now().plusDays(15));
            fluxoCaixa.registrarSaidaPrevista(600.0, "Aluguel", "Aluguel do consultório", LocalDate.now().plusDays(5));
        };
    }

    @Bean
    public InadimplenciaApplicationService inadimplenciaApplicationService(InadimplenciaRepository inadimplenciaRepository) {
        return new InadimplenciaApplicationService(inadimplenciaRepository);
    }
}
