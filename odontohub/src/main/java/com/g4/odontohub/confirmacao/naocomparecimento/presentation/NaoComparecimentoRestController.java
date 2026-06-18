package com.g4.odontohub.confirmacao.naocomparecimento.presentation;

import com.g4.odontohub.confirmacao.naocomparecimento.application.NaoComparecimentoApplicationService;
import com.g4.odontohub.confirmacao.naocomparecimento.domain.model.NaoComparecimento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** Camada de apresentação (REST) do contexto de Não Comparecimento — F17. */
@RestController
@RequestMapping("/api/nao-comparecimentos")
public class NaoComparecimentoRestController {

    private final NaoComparecimentoApplicationService applicationService;

    public NaoComparecimentoRestController(NaoComparecimentoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<NaoComparecimento> registrar(@RequestBody FaltaRequest request) {
        return ResponseEntity.ok(applicationService.registrarFalta(
                request.agendamentoId(), request.paciente(),
                LocalDate.parse(request.dataConsulta()), request.justificada(), request.justificativa()));
    }

    @GetMapping("/paciente/{paciente}/reincidente")
    public ResponseEntity<Boolean> reincidente(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.pacienteReincidente(paciente));
    }

    @GetMapping("/paciente/{paciente}/total")
    public ResponseEntity<Integer> total(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.totalFaltasDe(paciente));
    }

    public record FaltaRequest(Long agendamentoId, String paciente, String dataConsulta,
                               boolean justificada, String justificativa) {}
}
