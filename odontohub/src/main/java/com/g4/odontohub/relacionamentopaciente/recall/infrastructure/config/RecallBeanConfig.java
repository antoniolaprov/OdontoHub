package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.config;

import com.g4.odontohub.relacionamentopaciente.recall.application.RecallApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;
import com.g4.odontohub.relacionamentopaciente.followup.application.FollowupApplicationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root do contexto de Recall (F07): liga a aplicação ao adapter JPA
 * e integra o escalonamento de recall com o follow-up (F10).
 */
@Configuration
public class RecallBeanConfig {

    @Bean
    public RecallApplicationService recallApplicationService(
            RecallRepository repositorio, FollowupApplicationService followupService) {
        return new RecallApplicationService(repositorio, followupService);
    }

    /** Semeia recalls de exemplo para a fila não iniciar vazia (só se não houver nenhum). */
    @Bean
    public CommandLineRunner seedRecall(RecallApplicationService recallService) {
        return args -> {
            if (!recallService.todos().isEmpty()) {
                return;
            }
            recallService.processarGatilhoRecall("Pedro Alves", "Limpeza");
            recallService.processarGatilhoRecall("Ana Costa", "Restauração");
            recallService.processarGatilhoRecall("Lucia Ramos", "Avaliação");
        };
    }
}
