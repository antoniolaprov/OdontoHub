package com.g4.odontohub.cadastropaciente.infrastructure.config;

import com.g4.odontohub.cadastropaciente.application.PacienteApplicationService;
import com.g4.odontohub.cadastropaciente.domain.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CadastroPacienteBeanConfig {

    @Bean
    public PacienteApplicationService pacienteApplicationService(PacienteRepository repositorio) {
        return new PacienteApplicationService(repositorio);
    }

    /**
     * Semeia pacientes de exemplo (só se não houver nenhum). São os mesmos nomes
     * usados pelos seeds de Inadimplência/Pagamentos, para a demo ficar coerente
     * entre as telas — e o cadastro dispara os eventos que propagam o paciente
     * para os contextos de Agendamento e Inadimplência.
     */
    @Bean
    public CommandLineRunner seedPacientes(PacienteApplicationService pacienteService) {
        return args -> {
            if (!pacienteService.listarTodos().isEmpty()) {
                return;
            }
            pacienteService.cadastrarCompleto("Maria Santos", "111.111.111-11", "12/03/1985",
                    "(81) 99999-1111", "maria.santos@email.com", "Sistema");
            pacienteService.cadastrarCompleto("João Pereira", "222.222.222-22", "07/08/1990",
                    "(81) 99999-2222", "joao.pereira@email.com", "Sistema");
            pacienteService.cadastrarCompleto("Ana Costa", "333.333.333-33", "23/11/1978",
                    "(81) 99999-3333", "ana.costa@email.com", "Sistema");
            pacienteService.cadastrarCompleto("Pedro Alves", "444.444.444-44", "15/01/2000",
                    "(81) 99999-4444", "pedro.alves@email.com", "Sistema");
            pacienteService.cadastrarCompleto("Lucia Ramos", "555.555.555-55", "30/06/1995",
                    "(81) 99999-5555", "lucia.ramos@email.com", "Sistema");
        };
    }
}
