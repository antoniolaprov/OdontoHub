package com.g4.odontohub.financeiro.presentation;

import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Camada de apresentação (REST) de inadimplência e acordos — F09. */
@RestController
@RequestMapping("/api/inadimplencia")
public class InadimplenciaRestController {

    private final InadimplenciaApplicationService applicationService;

    public InadimplenciaRestController(InadimplenciaApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/parcelas-vencidas")
    public ResponseEntity<ParcelaCobranca> registrarVencida(@RequestBody ParcelaVencidaRequest r) {
        return ResponseEntity.ok(applicationService.registrarParcelaVencida(r.paciente(), r.valor(), r.diasAtraso()));
    }

    @GetMapping("/{paciente}/restrito")
    public ResponseEntity<Boolean> restrito(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.verificarRestricao(paciente));
    }

    @PostMapping("/{paciente}/acordo")
    public ResponseEntity<Acordo> firmarAcordo(@PathVariable String paciente, @RequestBody AcordoRequest r) {
        return ResponseEntity.ok(applicationService.firmarAcordo(paciente, r.quantidadeNovas(), r.valorNova()));
    }

    public record ParcelaVencidaRequest(String paciente, double valor, int diasAtraso) {}

    public record AcordoRequest(int quantidadeNovas, double valorNova) {}
}
