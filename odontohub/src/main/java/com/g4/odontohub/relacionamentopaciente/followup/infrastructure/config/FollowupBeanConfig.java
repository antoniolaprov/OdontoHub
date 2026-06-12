package com.g4.odontohub.relacionamentopaciente.followup.infrastructure.config;

import com.g4.odontohub.relacionamentopaciente.followup.application.FollowupApplicationService;
import com.g4.odontohub.relacionamentopaciente.followup.domain.repository.FollowupRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FollowupBeanConfig {

    @Bean
    public FollowupApplicationService followupApplicationService(FollowupRepository repositorio) {
        return new FollowupApplicationService(repositorio);
    }
}
