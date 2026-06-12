package com.g4.odontohub.relacionamentopaciente.recall.presentation;

import com.g4.odontohub.relacionamentopaciente.recall.application.RecallApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Camada de apresentação (REST) do contexto de Recall — F07. */
@RestController
@RequestMapping("/api/recalls")
public class RecallRestController {

    private final RecallApplicationService applicationService;

    public RecallRestController(RecallApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/gatilho")
    public ResponseEntity<Recall> dispararGatilho(@RequestBody RecallRequest request) {
        return ResponseEntity.ok(
                applicationService.processarGatilhoRecall(request.paciente(), request.procedimento()));
    }

    @GetMapping("/paciente/{paciente}")
    public ResponseEntity<Recall> consultar(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.buscarPorPaciente(paciente));
    }

    public record RecallRequest(String paciente, String procedimento) {}
}
