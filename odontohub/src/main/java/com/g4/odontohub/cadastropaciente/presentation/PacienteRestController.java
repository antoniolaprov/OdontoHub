package com.g4.odontohub.cadastropaciente.presentation;

import com.g4.odontohub.cadastropaciente.application.PacienteApplicationService;
import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;
import com.g4.odontohub.cadastropaciente.domain.model.StatusPaciente;
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

    /** Lista todos os pacientes, mais recente primeiro (leitura para o frontend). */
    @GetMapping
    public ResponseEntity<List<Paciente>> listar() {
        return ResponseEntity.ok(applicationService.listarTodos());
    }

    /** Identifica o paciente pelo id (único) — nome não é único, ver {@link #listar()}. */
    @PutMapping("/{id}/campo")
    public ResponseEntity<Paciente> atualizarCampo(@PathVariable Long id, @RequestBody AtualizarCampoRequest r) {
        return ResponseEntity.ok(applicationService.atualizarCadastro(
                new PacienteRegistroId(id), r.campo(), r.novoValor(), "Recepcionista"));
    }

    @PostMapping("/{id}/restringir")
    public ResponseEntity<Paciente> restringir(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.restringirPaciente(new PacienteRegistroId(id)));
    }

    /** Transição genérica de status — única forma de tirar um paciente de Restrito (ex.: voltar para Ativo). */
    @PostMapping("/{id}/status")
    public ResponseEntity<Paciente> atualizarStatus(@PathVariable Long id, @RequestBody StatusRequest r) {
        return ResponseEntity.ok(applicationService.atualizarStatus(
                new PacienteRegistroId(id), StatusPaciente.valueOf(r.status()), "Recepcionista"));
    }

    public record PacienteRequest(String nomeCompleto, String cpf, String dataNascimento,
                                  String telefone, String email) {}

    public record AtualizarCampoRequest(String campo, String novoValor) {}

    public record StatusRequest(String status) {}

    /** Mapeia erros de validação/regra de negócio para 400, em vez do 500 genérico padrão. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErroDeValidacao(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
