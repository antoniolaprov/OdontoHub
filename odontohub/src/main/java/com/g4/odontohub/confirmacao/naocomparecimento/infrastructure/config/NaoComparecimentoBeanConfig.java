package com.g4.odontohub.confirmacao.naocomparecimento.infrastructure.config;

import com.g4.odontohub.confirmacao.naocomparecimento.application.NaoComparecimentoApplicationService;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.repository.NaoComparecimentoRepository;
import com.g4.odontohub.relacionamentopaciente.churn.application.ChurnApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root do contexto de Não Comparecimento (F17): liga a aplicação ao adapter JPA
 * e integra as faltas com a análise de churn/absenteísmo (F11).
 */
@Configuration
public class NaoComparecimentoBeanConfig {

    @Bean
    public NaoComparecimentoApplicationService naoComparecimentoApplicationService(
            NaoComparecimentoRepository naoComparecimentoRepository, ChurnApplicationService churnService) {
        return new NaoComparecimentoApplicationService(naoComparecimentoRepository, churnService);
    }
}
