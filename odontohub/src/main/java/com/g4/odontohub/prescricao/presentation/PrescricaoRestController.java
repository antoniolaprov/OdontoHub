package com.g4.odontohub.prescricao.presentation;

import com.g4.odontohub.prescricao.application.PrescricaoApplicationService;
import com.g4.odontohub.prescricao.domain.model.ItemPrescricao;
import com.g4.odontohub.prescricao.domain.model.Prescricao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) do contexto de Prescrição — F8. */
@RestController
@RequestMapping("/api/prescricoes")
public class PrescricaoRestController {

    private final PrescricaoApplicationService applicationService;

    public PrescricaoRestController(PrescricaoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Prescricao> registrar(@RequestBody PrescricaoRequest request) {
        Prescricao p = applicationService.registrarPrescricao(
                request.paciente(), request.dentista(), request.itens(), request.observacoes());
        return ResponseEntity.ok(p);
    }

    @GetMapping("/paciente/{paciente}")
    public ResponseEntity<List<Prescricao>> historico(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.listarPorPaciente(paciente));
    }

    public record PrescricaoRequest(String paciente, String dentista,
                                    List<ItemPrescricao> itens, String observacoes) {}
}
