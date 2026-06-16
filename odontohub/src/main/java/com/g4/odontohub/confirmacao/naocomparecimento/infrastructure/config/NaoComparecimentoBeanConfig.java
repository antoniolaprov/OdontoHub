package com.g4.odontohub.confirmacao.naocomparecimento.infrastructure.config;

import com.g4.odontohub.confirmacao.naocomparecimento.application.NaoComparecimentoApplicationService;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.repository.NaoComparecimentoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root do contexto de Não Comparecimento (F14): liga a aplicação ao adapter JPA. */
@Configuration
public class NaoComparecimentoBeanConfig {

    @Bean
    public NaoComparecimentoApplicationService naoComparecimentoApplicationService(
            NaoComparecimentoRepository naoComparecimentoRepository) {
        return new NaoComparecimentoApplicationService(naoComparecimentoRepository);
    }
}
