package com.g4.odontohub.cadastropaciente.presentation;

import com.g4.odontohub.cadastropaciente.application.PacienteApplicationService;
import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) do contexto de Cadastro de Paciente — F13. */
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

    @GetMapping
    public ResponseEntity<List<Paciente>> listarTodos() {
        return ResponseEntity.ok(applicationService.listarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> atualizarCadastro(@PathVariable Long id, @RequestBody AtualizacaoCadastroRequest r) {
        return ResponseEntity.ok(applicationService.atualizarCadastro(
                new PacienteRegistroId(id), r.campo(), r.novoValor(), r.responsavel()));
    }

    @PostMapping("/{id}/restringir")
    public ResponseEntity<Paciente> restringir(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.restringirPaciente(new PacienteRegistroId(id)));
    }

    public record PacienteRequest(String nomeCompleto, String cpf, String dataNascimento,
                                  String telefone, String email) {}

    public record AtualizacaoCadastroRequest(String campo, String novoValor, String responsavel) {}
}
