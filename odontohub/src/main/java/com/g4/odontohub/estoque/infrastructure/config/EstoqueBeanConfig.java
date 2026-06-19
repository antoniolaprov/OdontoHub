package com.g4.odontohub.estoque.infrastructure.config;

import com.g4.odontohub.estoque.application.InstrumentoApplicationService;
import com.g4.odontohub.estoque.application.MaterialApplicationService;
import com.g4.odontohub.estoque.domain.repository.InstrumentoRepository;
import com.g4.odontohub.estoque.domain.repository.MaterialRepository;
import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EstoqueBeanConfig {

    @Bean
    public MaterialApplicationService materialApplicationService(
            FluxoCaixaApplicationService fluxoCaixaService, MaterialRepository materialRepository) {
        return new MaterialApplicationService(fluxoCaixaService, materialRepository);
    }

    /** Semeia materiais de exemplo para o estoque não iniciar vazio (só se não houver nenhum). */
    @Bean
    public CommandLineRunner seedMateriais(MaterialApplicationService materialService) {
        return args -> {
            if (!materialService.todos().isEmpty()) {
                return;
            }
            materialService.cadastrarMaterial("Luvas Descartáveis (cx)", "caixa", 150, 30);
            materialService.cadastrarMaterial("Anestésico Lidocaína 2%", "tubete", 8, 20);
            materialService.cadastrarMaterial("Algodão Hidrófilo (pacote)", "pacote", 35, 15);
            materialService.cadastrarMaterial("Resina Composta A2", "seringa", 3, 10);
            materialService.cadastrarMaterial("Fio de Sutura 4-0", "unidade", 12, 8);
            materialService.cadastrarMaterial("Máscara Tripla Camada (cx)", "caixa", 200, 40);
        };
    }

    @Bean
    public InstrumentoApplicationService instrumentoApplicationService(InstrumentoRepository instrumentoRepository) {
        return new InstrumentoApplicationService(instrumentoRepository);
    }
}
