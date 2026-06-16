package com.g4.odontohub.confirmacao.lembrete.infrastructure.config;

import com.g4.odontohub.confirmacao.lembrete.application.LembreteApplicationService;
import com.g4.odontohub.confirmacao.lembrete.domain.repository.LembreteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root do contexto de Lembrete (F13): liga a aplicação ao adapter JPA. */
@Configuration
public class LembreteBeanConfig {

    @Bean
    public LembreteApplicationService lembreteApplicationService(LembreteRepository lembreteRepository) {
        return new LembreteApplicationService(lembreteRepository);
    }
}
