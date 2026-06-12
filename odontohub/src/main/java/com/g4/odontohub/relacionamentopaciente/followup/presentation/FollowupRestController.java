package com.g4.odontohub.relacionamentopaciente.followup.presentation;

import com.g4.odontohub.relacionamentopaciente.followup.application.FollowupApplicationService;
import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupPosOperatorio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) do contexto de Follow-up — F10. */
@RestController
@RequestMapping("/api/followups")
public class FollowupRestController {

    private final FollowupApplicationService applicationService;

    public FollowupRestController(FollowupApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/gatilho")
    public ResponseEntity<Void> processarGatilho(@RequestBody GatilhoRequest request) {
        applicationService.processarGatilhos(request.paciente(), request.tipoProcedimento());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/checklist")
    public ResponseEntity<Void> registrarChecklist(@RequestBody ChecklistRequest request) {
        applicationService.registrarChecklist(request.paciente(), request.sangramento(), request.nivelDor(),
                request.observacoes(), request.responsavel(), request.dentistaResponsavel());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paciente/{paciente}")
    public ResponseEntity<List<FollowupPosOperatorio>> tarefas(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.tarefasDe(paciente));
    }

    public record GatilhoRequest(String paciente, String tipoProcedimento) {}

    public record ChecklistRequest(String paciente, boolean sangramento, int nivelDor,
                                   String observacoes, String responsavel, String dentistaResponsavel) {}
}
