package com.g4.odontohub.financeiro.presentation;

import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Camada de apresentação (REST) do fluxo de caixa — F04. */
@RestController
@RequestMapping("/api/fluxo-caixa")
public class FluxoCaixaRestController {

    private final FluxoCaixaApplicationService applicationService;

    public FluxoCaixaRestController(FluxoCaixaApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/saidas")
    public ResponseEntity<LancamentoFinanceiro> registrarSaida(@RequestBody SaidaRequest r) {
        return ResponseEntity.ok(applicationService.registrarSaidaManual(r.valor(), r.categoria(), r.descricao()));
    }

    /** Registra uma entrada futura prevista (parcela a vencer). */
    @PostMapping("/entradas-previstas")
    public ResponseEntity<LancamentoFinanceiro> registrarEntradaPrevista(@RequestBody EntradaPrevistaRequest r) {
        return ResponseEntity.ok(
                applicationService.registrarEntradaPrevista(r.valor(), r.descricao(), LocalDate.parse(r.vencimento())));
    }

    /** Registra uma saída futura prevista (despesa planejada). */
    @PostMapping("/saidas-previstas")
    public ResponseEntity<LancamentoFinanceiro> registrarSaidaPrevista(@RequestBody SaidaPrevistaRequest r) {
        return ResponseEntity.ok(applicationService.registrarSaidaPrevista(
                r.valor(), r.categoria(), r.descricao(), LocalDate.parse(r.data())));
    }

    @GetMapping("/lancamentos")
    public ResponseEntity<List<LancamentoFinanceiro>> lancamentos() {
        return ResponseEntity.ok(applicationService.getLancamentos());
    }

    /** Saldo atual: apenas lançamentos confirmados. */
    @GetMapping("/saldo-atual")
    public ResponseEntity<Double> saldoAtual() {
        return ResponseEntity.ok(applicationService.calcularSaldoAtual());
    }

    /** Saldo projetado: confirmados + parcelas a vencer − saídas previstas. */
    @GetMapping("/saldo-projetado")
    public ResponseEntity<Double> saldoProjetado() {
        return ResponseEntity.ok(applicationService.calcularSaldoProjetado());
    }

    public record SaidaRequest(double valor, String categoria, String descricao) {}

    public record EntradaPrevistaRequest(double valor, String descricao, String vencimento) {}

    public record SaidaPrevistaRequest(double valor, String categoria, String descricao, String data) {}
}
