package com.g4.odontohub.agendamento.presentation;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.AgendamentoId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/** Camada de apresentação (REST) do contexto de Agendamento — F01. */
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoRestController {

    private final AgendamentoApplicationService applicationService;

    public AgendamentoRestController(AgendamentoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Agendamento> registrar(@RequestBody AgendamentoRequest r) {
        applicationService.cadastrarPaciente(r.paciente());
        applicationService.cadastrarDentista(r.dentista());
        Agendamento ag = applicationService.registrarAgendamento(r.paciente(), r.dentista(), r.dataHora());
        return ResponseEntity.ok(ag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> consultar(@PathVariable Long id) {
        Agendamento ag = applicationService.buscarPorId(new AgendamentoId(id));
        return ag == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(ag);
    }

    public record AgendamentoRequest(String paciente, String dentista,
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHora) {}
}
