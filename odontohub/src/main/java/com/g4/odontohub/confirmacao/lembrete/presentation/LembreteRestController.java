package com.g4.odontohub.confirmacao.lembrete.presentation;

import com.g4.odontohub.confirmacao.lembrete.application.LembreteApplicationService;
import com.g4.odontohub.confirmacao.lembrete.domain.model.Lembrete;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Camada de apresentação (REST) do contexto de Lembrete — F13. */
@RestController
@RequestMapping("/api/lembretes")
public class LembreteRestController {

    private final LembreteApplicationService applicationService;

    public LembreteRestController(LembreteApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Lembrete> gerar(@RequestBody LembreteRequest request) {
        return ResponseEntity.ok(
                applicationService.gerarLembrete(request.agendamentoId(), request.paciente(), request.canal()));
    }

    @PostMapping("/{agendamentoId}/enviar")
    public ResponseEntity<Lembrete> enviar(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(applicationService.enviar(agendamentoId));
    }

    @PostMapping("/{agendamentoId}/confirmar")
    public ResponseEntity<Lembrete> confirmar(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(applicationService.confirmar(agendamentoId));
    }

    @PostMapping("/{agendamentoId}/recusar")
    public ResponseEntity<Lembrete> recusar(@PathVariable Long agendamentoId, @RequestBody RecusaRequest request) {
        return ResponseEntity.ok(applicationService.recusar(agendamentoId, request.motivo()));
    }

    @GetMapping("/{agendamentoId}")
    public ResponseEntity<Lembrete> consultar(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(applicationService.buscarPorAgendamento(agendamentoId));
    }

    public record LembreteRequest(Long agendamentoId, String paciente, String canal) {}

    public record RecusaRequest(String motivo) {}
}
