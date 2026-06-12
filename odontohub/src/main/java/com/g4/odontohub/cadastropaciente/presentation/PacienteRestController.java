package com.g4.odontohub.cadastropaciente.presentation;

import com.g4.odontohub.cadastropaciente.application.PacienteApplicationService;
import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Camada de apresentação (REST) do contexto de Cadastro de Paciente — F15. */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteRestController {

    private final PacienteApplicationService applicationService;

    public PacienteRestController(PacienteApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Paciente> cadastrarCompleto(@RequestBody PacienteRequest r) {
        return ResponseEntity.ok(applicationService.cadastrarCompleto(
                r.nomeCompleto(), r.cpf(), r.dataNascimento(), r.telefone(), r.email(), "Recepcionista"));
    }

    @PostMapping("/rapido")
    public ResponseEntity<Paciente> cadastrarRapido(@RequestBody PacienteRequest r) {
        return ResponseEntity.ok(applicationService.cadastrarRapido(r.nomeCompleto(), r.telefone(), "Recepcionista"));
    }

    @GetMapping("/{nome}")
    public ResponseEntity<Paciente> buscar(@PathVariable String nome) {
        return ResponseEntity.ok(applicationService.buscarPorNome(nome));
    }

    public record PacienteRequest(String nomeCompleto, String cpf, String dataNascimento,
                                  String telefone, String email) {}
}
