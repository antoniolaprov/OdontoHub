package com.g4.odontohub.medicamento.infrastructure.config;

import com.g4.odontohub.medicamento.application.MedicamentoApplicationService;
import com.g4.odontohub.medicamento.domain.repository.MedicamentoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MedicamentoBeanConfig {

    @Bean
    public MedicamentoApplicationService medicamentoApplicationService(MedicamentoRepository repositorio) {
        return new MedicamentoApplicationService(repositorio);
    }

    /** Semeia o catálogo de medicamentos (só se estiver vazio). Classes farmacológicas reconhecidas pelo domínio. */
    @Bean
    public CommandLineRunner seedMedicamentos(MedicamentoApplicationService medicamentoService) {
        return args -> {
            if (!medicamentoService.listarParaSelecao().isEmpty()) {
                return;
            }
            medicamentoService.cadastrar("Amoxil 500mg", "Amoxicilina", "Antibiótico", "Beta-lactâmicos");
            medicamentoService.cadastrar("Ibupril 600mg", "Ibuprofeno", "Anti-inflamatório", "AINEs");
            medicamentoService.cadastrar("Dipirona Sódica 500mg", "Dipirona", "Analgésico", "Analgésicos");
            medicamentoService.cadastrar("Predsim 20mg", "Prednisolona", "Corticoide", "Corticosteroides");
            medicamentoService.cadastrar("Tylenol 750mg", "Paracetamol", "Analgésico", "Analgésicos");
        };
    }
}
