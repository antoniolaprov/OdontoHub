package com.g4.odontohub.equipe.infrastructure.config;

import com.g4.odontohub.equipe.application.ColaboradorApplicationService;
import com.g4.odontohub.equipe.domain.repository.ColaboradorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EquipeBeanConfig {

    @Bean
    public ColaboradorApplicationService colaboradorApplicationService(ColaboradorRepository repositorio) {
        return new ColaboradorApplicationService(repositorio);
    }

    /** Semeia a equipe de exemplo para a tela de Equipe não iniciar vazia (só se não houver ninguém). */
    @Bean
    public CommandLineRunner seedEquipe(ColaboradorApplicationService colaboradorService) {
        return args -> {
            if (!colaboradorService.todos().isEmpty()) {
                return;
            }
            colaboradorService.cadastrar("Dra. Helena Martins", "100.100.100-10", "(81) 98888-1000", "Especialista");
            colaboradorService.cadastrar("Ana Paula Souza", "200.200.200-20", "(81) 98888-2000", "Recepcionista");
            colaboradorService.cadastrar("Carlos Eduardo Lima", "300.300.300-30", "(81) 98888-3000", "Auxiliar");
            colaboradorService.cadastrar("Roberto Diretor", "400.400.400-40", "(81) 98888-4000", "Administrador");
        };
    }
}
