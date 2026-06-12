package com.g4.odontohub.equipe.presentation;

import com.g4.odontohub.equipe.application.ColaboradorApplicationService;
import com.g4.odontohub.equipe.domain.model.Colaborador;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) do contexto de Equipe — F12. */
@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorRestController {

    private final ColaboradorApplicationService applicationService;

    public ColaboradorRestController(ColaboradorApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Colaborador> cadastrar(@RequestBody ColaboradorRequest request) {
        Colaborador c = applicationService.cadastrar(
                request.nomeCompleto(), request.cpf(), request.telefone(), request.funcao());
        return ResponseEntity.ok(c);
    }

    @PostMapping("/{nome}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable String nome) {
        applicationService.desativar(nome);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{nome}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable String nome) {
        applicationService.reativar(nome);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/responsaveis-esterilizacao")
    public ResponseEntity<List<Colaborador>> responsaveisEsterilizacao() {
        return ResponseEntity.ok(applicationService.listarResponsaveisEsterilizacao());
    }

    public record ColaboradorRequest(String nomeCompleto, String cpf, String telefone, String funcao) {}
}
