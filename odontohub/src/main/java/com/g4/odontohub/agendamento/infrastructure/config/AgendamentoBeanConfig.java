package com.g4.odontohub.agendamento.infrastructure.config;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.repository.AgendamentoRepository;
import com.g4.odontohub.cadastropaciente.domain.event.PacienteCadastrado;
import com.g4.odontohub.cadastropaciente.domain.event.PacienteCadastradoRapido;
import com.g4.odontohub.shared.DomainEventPublisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class AgendamentoBeanConfig {

    @Bean
    public AgendamentoApplicationService agendamentoApplicationService(AgendamentoRepository repositorio) {
        AgendamentoApplicationService service = new AgendamentoApplicationService(repositorio);
        // Integração cross-context (ACL) via eventos de domínio: um paciente
        // cadastrado no contexto de Cadastro de Pacientes (F13) passa a ser
        // reconhecido pelo Agendamento (F1), sem acoplar os dois contextos.
        // A inscrição fica só aqui (raiz de composição de produção) — os testes
        // BDD usam o construtor sem-arg + DomainEventPublisher.reset(), então
        // nada vaza entre cenários.
        DomainEventPublisher.subscribe(PacienteCadastrado.class,
                e -> service.cadastrarPaciente(e.nomeCompleto()));
        DomainEventPublisher.subscribe(PacienteCadastradoRapido.class,
                e -> service.cadastrarPaciente(e.nomeCompleto()));
        return service;
    }

    /** Semeia agendamentos de exemplo para a agenda não iniciar vazia (só se não houver nenhum). */
    @Bean
    public CommandLineRunner seedAgendamentos(AgendamentoApplicationService agendamentoService) {
        return args -> {
            if (!agendamentoService.listarTodos().isEmpty()) {
                return;
            }
            agendamentoService.cadastrarDentista("Dra. Helena Martins");
            agendamentoService.cadastrarPaciente("Maria Santos");
            agendamentoService.cadastrarPaciente("João Pereira");
            agendamentoService.cadastrarPaciente("Ana Costa");

            LocalDateTime base = LocalDateTime.of(LocalDateTime.now().toLocalDate().plusDays(1), LocalTime.of(9, 0));
            agendamentoService.registrarAgendamento("Maria Santos", "Dra. Helena Martins", base);
            agendamentoService.registrarAgendamento("João Pereira", "Dra. Helena Martins", base.plusHours(1));
            agendamentoService.registrarAgendamento("Ana Costa", "Dra. Helena Martins", base.plusDays(1));
        };
    }
}
