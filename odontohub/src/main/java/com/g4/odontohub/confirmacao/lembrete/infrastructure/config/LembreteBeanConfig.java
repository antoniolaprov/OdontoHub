package com.g4.odontohub.confirmacao.lembrete.infrastructure.config;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.confirmacao.lembrete.application.LembreteApplicationService;
import com.g4.odontohub.confirmacao.lembrete.domain.repository.LembreteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root do contexto de Lembrete (F16): liga a aplicação ao adapter JPA
 * e integra a confirmação do paciente com o contexto de Agendamento (F1).
 */
@Configuration
public class LembreteBeanConfig {

    @Bean
    public LembreteApplicationService lembreteApplicationService(
            LembreteRepository lembreteRepository, AgendamentoApplicationService agendamentoService) {
        return new LembreteApplicationService(lembreteRepository, agendamentoService);
    }
}
