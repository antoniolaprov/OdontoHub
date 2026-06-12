package com.g4.odontohub.relacionamentopaciente.churn.presentation;

import com.g4.odontohub.relacionamentopaciente.churn.application.ChurnApplicationService;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.StatusChurn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Camada de apresentação (REST) do contexto de Churn — F11. */
@RestController
@RequestMapping("/api/churn")
public class ChurnRestController {

    private final ChurnApplicationService applicationService;

    public ChurnRestController(ChurnApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/recalcular")
    public ResponseEntity<Void> recalcular() {
        applicationService.recalcular();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/paciente/{paciente}/status")
    public ResponseEntity<StatusChurn> status(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.statusDe(paciente));
    }

    @GetMapping("/receita-perdida")
    public ResponseEntity<Double> receitaPerdida(@RequestParam int horas) {
        return ResponseEntity.ok(applicationService.calcularReceitaPerdida(horas));
    }

    public record DadosPacienteRequest(String paciente, boolean agendamentoFuturo,
                                       int mesesSemRetorno, boolean planoAtivo) {}
}
