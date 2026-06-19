package com.g4.odontohub.clinica.application;

import com.g4.odontohub.clinica.domain.event.ClinicaCadastrada;
import com.g4.odontohub.clinica.domain.model.Clinica;
import com.g4.odontohub.clinica.domain.repository.ClinicaRepository;
import com.g4.odontohub.clinica.domain.service.ClinicaService;
import com.g4.odontohub.clinica.domain.service.CodificadorSenha;
import com.g4.odontohub.clinica.infrastructure.persistence.InMemoryClinicaRepository;
import com.g4.odontohub.clinica.infrastructure.security.BCryptCodificadorSenha;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.List;

/** Serviço de aplicação do contexto de Clínica (F18) — orquestra domínio + eventos. */
public class ClinicaApplicationService {

    private final ClinicaService service;

    /** Composição padrão para os testes BDD: repositório em memória + BCrypt. */
    public ClinicaApplicationService() {
        this(new InMemoryClinicaRepository(), new BCryptCodificadorSenha());
    }

    public ClinicaApplicationService(ClinicaRepository repositorio, CodificadorSenha codificador) {
        this.service = new ClinicaService(repositorio, codificador);
    }

    public Clinica cadastrar(String nome, String cnpj, String email, String senha) {
        Clinica clinica = service.cadastrar(nome, cnpj, email, senha);
        DomainEventPublisher.publish(new ClinicaCadastrada(
                clinica.getId(), clinica.getNome(), clinica.getCnpj(), clinica.getEmail()));
        return clinica;
    }

    public Clinica autenticar(String email, String senha) {
        return service.autenticar(email, senha);
    }

    public boolean emailJaCadastrado(String email) {
        return service.emailJaCadastrado(email);
    }

    public boolean cnpjJaCadastrado(String cnpj) {
        return service.cnpjJaCadastrado(cnpj);
    }

    public Clinica buscarPorEmail(String email) {
        return service.buscarPorEmail(email);
    }

    public List<Clinica> todas() {
        return service.todas();
    }
}
