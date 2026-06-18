package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.config;

import com.g4.odontohub.relacionamentopaciente.recall.application.RecallApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.domain.repository.RecallRepository;
import com.g4.odontohub.relacionamentopaciente.followup.application.FollowupApplicationService;
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
}
