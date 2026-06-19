package com.g4.odontohub.clinica.infrastructure.config;

import com.g4.odontohub.clinica.application.ClinicaApplicationService;
import com.g4.odontohub.clinica.domain.repository.ClinicaRepository;
import com.g4.odontohub.clinica.domain.service.CodificadorSenha;
import com.g4.odontohub.clinica.infrastructure.security.BCryptCodificadorSenha;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composição (raiz) do contexto de Clínica (F18) para o ambiente Spring. */
@Configuration
public class ClinicaBeanConfig {

    @Bean
    public CodificadorSenha codificadorSenha() {
        return new BCryptCodificadorSenha();
    }

    @Bean
    public ClinicaApplicationService clinicaApplicationService(ClinicaRepository repositorio,
                                                              CodificadorSenha codificador) {
        return new ClinicaApplicationService(repositorio, codificador);
    }

    /** Semeia uma clínica de demonstração para permitir login imediato. */
    @Bean
    public CommandLineRunner seedClinicaDemo(ClinicaApplicationService clinicaService) {
        return args -> {
            if (!clinicaService.emailJaCadastrado("admin@odontohub.com")) {
                clinicaService.cadastrar(
                        "Clínica OdontoHub Demo",
                        "12.345.678/0001-90",
                        "admin@odontohub.com",
                        "odonto123");
            }
        };
    }
}
