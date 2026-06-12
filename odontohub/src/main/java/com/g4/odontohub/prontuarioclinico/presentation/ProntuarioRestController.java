package com.g4.odontohub.prontuarioclinico.presentation;

import com.g4.odontohub.prontuarioclinico.application.ProntuarioApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Camada de apresentação (REST) do prontuário clínico — F02/F03. */
@RestController
@RequestMapping("/api/prontuarios")
public class ProntuarioRestController {

    private final ProntuarioApplicationService applicationService;

    public ProntuarioRestController(ProntuarioApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/anamnese")
    public ResponseEntity<Void> registrarAnamnese(@RequestBody AnamneseRequest r) {
        applicationService.getAnamneseService().registrarAnamnese(
                r.pacienteId(), r.alergias(), r.contraindicacoes(), r.condicoesSistemicas(), r.responsavel());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/anamnese/{pacienteId}/existe")
    public ResponseEntity<Boolean> anamneseExiste(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(applicationService.getAnamneseService().anamneseExiste(pacienteId));
    }

    @PostMapping("/planos")
    public ResponseEntity<Void> criarPlano(@RequestBody PlanoRequest r) {
        applicationService.getPlanoTratamentoService().criarPlano(r.pacienteId(), r.dentistaId());
        return ResponseEntity.noContent().build();
    }

    public record AnamneseRequest(Long pacienteId, String alergias, String contraindicacoes,
                                  String condicoesSistemicas, String responsavel) {}

    public record PlanoRequest(Long pacienteId, Long dentistaId) {}
}
