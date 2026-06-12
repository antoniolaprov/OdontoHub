package com.g4.odontohub.prontuarioclinico.infrastructure.config;

import com.g4.odontohub.prontuarioclinico.application.AnamneseRepository;
import com.g4.odontohub.prontuarioclinico.application.InMemoryPlanoTratamentoRepository;
import com.g4.odontohub.prontuarioclinico.application.ProntuarioApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProntuarioBeanConfig {

    /**
     * Anamnese (F02) persiste em JPA/H2; Plano de Tratamento (F03) usa adapter
     * in-memory por ser o agregado mais complexo (procedimentos aninhados, logs,
     * janela de imutabilidade de 24h) — conversão JPA pendente.
     */
    @Bean
    public ProntuarioApplicationService prontuarioApplicationService(AnamneseRepository anamneseRepository) {
        return new ProntuarioApplicationService(anamneseRepository, new InMemoryPlanoTratamentoRepository());
    }
}
