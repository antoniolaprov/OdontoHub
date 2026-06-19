package com.g4.odontohub.financeiro.presentation;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) do fluxo de caixa — F04. */
@RestController
@RequestMapping("/api/fluxo-caixa")
public class FluxoCaixaRestController {

    private final FluxoCaixaApplicationService applicationService;

    public FluxoCaixaRestController(FluxoCaixaApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/entradas")
    public ResponseEntity<LancamentoFinanceiro> registrarEntrada(@RequestBody EntradaRequest r) {
        return ResponseEntity.ok(applicationService.registrarEntradaManual(r.valor(), r.descricao()));
    }

    @PostMapping("/saidas")
    public ResponseEntity<LancamentoFinanceiro> registrarSaida(@RequestBody SaidaRequest r) {
        return ResponseEntity.ok(applicationService.registrarSaidaManual(r.valor(), r.categoria(), r.descricao()));
    }

    @GetMapping("/lancamentos")
    public ResponseEntity<List<LancamentoFinanceiro>> lancamentos() {
        return ResponseEntity.ok(applicationService.getLancamentos());
    }

    @GetMapping("/saldo-projetado")
    public ResponseEntity<Double> saldoProjetado() {
        return ResponseEntity.ok(applicationService.calcularSaldoProjetado());
    }

    public record EntradaRequest(double valor, String descricao) {}

    public record SaidaRequest(double valor, String categoria, String descricao) {}
}
