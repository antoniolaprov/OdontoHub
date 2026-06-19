package com.g4.odontohub.clinica.presentation;

import com.g4.odontohub.clinica.application.ClinicaApplicationService;
import com.g4.odontohub.clinica.domain.model.Clinica;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) do contexto de Clínica — F18 (Cadastro/Login). */
@RestController
@RequestMapping("/api/clinicas")
public class ClinicaRestController {

    private final ClinicaApplicationService applicationService;

    public ClinicaRestController(ClinicaApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** Cadastra a clínica. Erros de validação/duplicidade viram 400 com mensagem. */
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody CadastroRequest r) {
        try {
            Clinica c = applicationService.cadastrar(r.nome(), r.cnpj(), r.email(), r.senha());
            return ResponseEntity.status(HttpStatus.CREATED).body(ClinicaResponse.from(c));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErroResponse(e.getMessage()));
        }
    }

    /** Autentica a clínica. Credenciais inválidas → 401. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest r) {
        try {
            Clinica c = applicationService.autenticar(r.email(), r.senha());
            return ResponseEntity.ok(ClinicaResponse.from(c));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErroResponse(e.getMessage()));
        }
    }

    /** Lista as clínicas cadastradas (sem expor o hash da senha). */
    @GetMapping
    public ResponseEntity<List<ClinicaResponse>> listar() {
        return ResponseEntity.ok(applicationService.todas().stream().map(ClinicaResponse::from).toList());
    }

    public record CadastroRequest(String nome, String cnpj, String email, String senha) {}

    public record LoginRequest(String email, String senha) {}

    public record ErroResponse(String erro) {}

    /** DTO de saída — nunca inclui o hash da senha. */
    public record ClinicaResponse(Long id, String nome, String cnpj, String email, String status) {
        static ClinicaResponse from(Clinica c) {
            return new ClinicaResponse(c.getId().id(), c.getNome(), c.getCnpj(),
                    c.getEmail(), c.getStatus().name());
        }
    }
}
