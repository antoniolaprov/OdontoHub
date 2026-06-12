package com.g4.odontohub.agendamento.infrastructure.config;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.repository.AgendamentoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgendamentoBeanConfig {

    @Bean
    public AgendamentoApplicationService agendamentoApplicationService(AgendamentoRepository repositorio) {
        return new AgendamentoApplicationService(repositorio);
    }
}
