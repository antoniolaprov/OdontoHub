package com.g4.odontohub.relacionamentopaciente.recall.presentation;

import com.g4.odontohub.relacionamentopaciente.recall.application.RecallApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** Fila de recall ordenada por prioridade clínica (maior risco primeiro). */
    @GetMapping("/fila-priorizada")
    public ResponseEntity<List<Recall>> filaPriorizada() {
        return ResponseEntity.ok(applicationService.filaPriorizada());
    }

    /** Registra uma tentativa de contato (SLA); retorna se o caso foi escalonado. */
    @PostMapping("/paciente/{paciente}/tentativa-contato")
    public ResponseEntity<Boolean> registrarTentativaContato(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.registrarTentativaContato(paciente));
    }

    /** Taxa de conversão da fila de recall (convertidos / disparados). */
    @GetMapping("/metricas/taxa-conversao")
    public ResponseEntity<Double> taxaConversao() {
        return ResponseEntity.ok(applicationService.taxaConversao());
    }

    public record RecallRequest(String paciente, String procedimento) {}
}
