package com.g4.odontohub.pagamento.presentation;

import com.g4.odontohub.pagamento.application.PagamentoApplicationService;
import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Camada de apresentação (REST) do contexto de Pagamento — F17.
 * Delega as operações para a camada de aplicação.
 */
@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoRestController {

    private final PagamentoApplicationService applicationService;

    public PagamentoRestController(PagamentoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/parcelas")
    public ResponseEntity<ParcelaPagavel> criarParcela(@RequestBody ParcelaRequest request) {
        ParcelaPagavel parcela = applicationService.criarParcela(request.referencia(), request.valor());
        return ResponseEntity.ok(parcela);
    }

    @PostMapping("/presencial")
    public ResponseEntity<Pagamento> registrarPresencial(@RequestBody PagamentoRequest request) {
        Pagamento pagamento = applicationService.registrarPagamentoPresencial(
                request.referencia(), request.valor(), LocalDate.now(), request.forma());
        return ResponseEntity.ok(pagamento);
    }

    @PostMapping("/aguardando")
    public ResponseEntity<Pagamento> lancarAguardando(@RequestBody PagamentoRequest request) {
        Pagamento pagamento = applicationService.lancarAguardandoComprovante(request.referencia(), request.forma());
        return ResponseEntity.ok(pagamento);
    }

    @GetMapping("/parcelas/{referencia}")
    public ResponseEntity<ParcelaPagavel> consultarParcela(@PathVariable String referencia) {
        return ResponseEntity.ok(applicationService.parcela(referencia));
    }

    @GetMapping("/parcelas/{referencia}/comprovante")
    public ResponseEntity<Boolean> comprovanteDisponivel(@PathVariable String referencia) {
        return ResponseEntity.ok(applicationService.comprovanteDisponivel(referencia));
    }

    public record ParcelaRequest(String referencia, double valor) {}

    public record PagamentoRequest(String referencia, double valor, String forma) {}
}
