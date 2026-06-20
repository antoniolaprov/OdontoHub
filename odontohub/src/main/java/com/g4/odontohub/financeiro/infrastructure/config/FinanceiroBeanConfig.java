package com.g4.odontohub.financeiro.infrastructure.config;

import com.g4.odontohub.cadastropaciente.domain.event.PacienteCadastrado;
import com.g4.odontohub.cadastropaciente.domain.event.PacienteCadastradoRapido;
import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;
import com.g4.odontohub.financeiro.domain.repository.LancamentoRepository;
import com.g4.odontohub.shared.DomainEventPublisher;
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
        InadimplenciaApplicationService service = new InadimplenciaApplicationService(inadimplenciaRepository);
        // Cross-context via eventos: paciente cadastrado no F13 passa a ser
        // conhecido pela Inadimplência (F9), para cobranças/acordos operarem
        // sobre pacientes reais. Inscrição só na raiz de composição de produção.
        DomainEventPublisher.subscribe(PacienteCadastrado.class,
                e -> service.registrarPaciente(e.nomeCompleto()));
        DomainEventPublisher.subscribe(PacienteCadastradoRapido.class,
                e -> service.registrarPaciente(e.nomeCompleto()));
        return service;
    }

    /** Semeia inadimplentes de exemplo para a tela de Acordos não iniciar vazia (só se não houver nenhum). */
    @Bean
    public CommandLineRunner seedInadimplencia(InadimplenciaApplicationService inadimplencia) {
        return args -> {
            if (!inadimplencia.pacientes().isEmpty()) {
                return;
            }
            inadimplencia.registrarParcelaVencida("Maria Santos", 600.0, 45);
            inadimplencia.registrarParcelaVencida("Maria Santos", 600.0, 15);
            inadimplencia.registrarParcelaVencida("Pedro Alves", 300.0, 10);
            inadimplencia.registrarParcelaVencida("Lucia Ramos", 800.0, 60);
        };
    }
}
