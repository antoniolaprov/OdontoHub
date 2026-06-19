package com.g4.odontohub.cadastropaciente.application;

import com.g4.odontohub.cadastropaciente.domain.event.CadastroPacienteAtualizado;
import com.g4.odontohub.cadastropaciente.domain.event.PacienteCadastrado;
import com.g4.odontohub.cadastropaciente.domain.event.PacienteCadastradoRapido;
import com.g4.odontohub.cadastropaciente.domain.event.PacienteRestrito;
import com.g4.odontohub.cadastropaciente.domain.model.AlteracaoCadastral;
import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;
import com.g4.odontohub.cadastropaciente.domain.repository.PacienteRepository;
import com.g4.odontohub.cadastropaciente.domain.service.PacienteService;
import com.g4.odontohub.cadastropaciente.infrastructure.persistence.InMemoryPacienteRepository;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.List;

public class PacienteApplicationService {

    private final PacienteService service;

    public PacienteApplicationService() {
        this(new InMemoryPacienteRepository());
    }

    public PacienteApplicationService(PacienteRepository repositorio) {
        this.service = new PacienteService(repositorio);
    }

    public Paciente cadastrarCompleto(String nomeCompleto, String cpf, String dataNascimento,
                                      String telefone, String email, String responsavel) {
        service.cadastrarCompleto(nomeCompleto, cpf, dataNascimento, telefone, email, responsavel);
        Paciente paciente = service.getUltimoPaciente();
        DomainEventPublisher.publish(new PacienteCadastrado(
                paciente.getId(), paciente.getNomeCompleto(), paciente.getCpf(), paciente.getStatus()));
        return paciente;
    }

    public Paciente cadastrarRapido(String nomeCompleto, String telefone, String responsavel) {
        service.cadastrarRapido(nomeCompleto, telefone, responsavel);
        Paciente paciente = service.getUltimoPaciente();
        DomainEventPublisher.publish(new PacienteCadastradoRapido(
                paciente.getId(), paciente.getNomeCompleto(), paciente.getTelefone()));
        return paciente;
    }

    public Paciente atualizarCadastro(PacienteRegistroId pacienteId, String campo, String novoValor, String responsavel) {
        service.atualizarCadastro(pacienteId, campo, novoValor, responsavel);
        Paciente paciente = service.buscarPorId(pacienteId);
        AlteracaoCadastral alteracao = paciente.getHistoricoAlteracoes()
                .get(paciente.getHistoricoAlteracoes().size() - 1);
        DomainEventPublisher.publish(new CadastroPacienteAtualizado(pacienteId, alteracao));
        return paciente;
    }

    public Paciente restringirPaciente(PacienteRegistroId pacienteId) {
        service.restringirPaciente(pacienteId);
        Paciente paciente = service.buscarPorId(pacienteId);
        DomainEventPublisher.publish(new PacienteRestrito(pacienteId));
        return paciente;
    }

    public boolean cpfJaCadastrado(String cpf) {
        return service.cpfJaCadastrado(cpf);
    }

    public boolean emailJaCadastrado(String email) {
        return service.emailJaCadastrado(email);
    }

    public Paciente buscarPorNome(String nomeCompleto) {
        return service.buscarPorNome(nomeCompleto);
    }

    public List<Paciente> listarTodos() {
        return service.listarTodos();
    }
}
