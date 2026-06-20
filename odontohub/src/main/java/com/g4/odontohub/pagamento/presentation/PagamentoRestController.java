package com.g4.odontohub.pagamento.presentation;

import com.g4.odontohub.pagamento.application.PagamentoApplicationService;
import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.ParcelaPagavel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    /** Confirma um lançamento que estava aguardando comprovante (baixa a parcela). */
    @PostMapping("/confirmar")
    public ResponseEntity<Pagamento> confirmar(@RequestBody ConfirmarRequest request) {
        Pagamento pagamento = applicationService.confirmarPagamento(
                request.referencia(), request.valor(), LocalDate.now());
        return ResponseEntity.ok(pagamento);
    }

    /** Cancela um lançamento aguardando comprovante (exige justificativa). */
    @PostMapping("/cancelar")
    public ResponseEntity<Void> cancelar(@RequestBody CancelarRequest request) {
        applicationService.cancelarLancamentoAguardando(request.referencia(), request.justificativa());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parcelas/{referencia}")
    public ResponseEntity<ParcelaPagavel> consultarParcela(@PathVariable String referencia) {
        return ResponseEntity.ok(applicationService.parcela(referencia));
    }

    @GetMapping("/parcelas/{referencia}/comprovante")
    public ResponseEntity<Boolean> comprovanteDisponivel(@PathVariable String referencia) {
        return ResponseEntity.ok(applicationService.comprovanteDisponivel(referencia));
    }

    /** Lista todas as parcelas pagáveis (leitura para o frontend). */
    @GetMapping("/parcelas")
    public ResponseEntity<List<ParcelaResponse>> listarParcelas() {
        List<ParcelaResponse> lista = applicationService.parcelas().stream()
                .map(ParcelaResponse::de)
                .toList();
        return ResponseEntity.ok(lista);
    }

    /** Lista o histórico de pagamentos (confirmados, aguardando e cancelados). */
    @GetMapping
    public ResponseEntity<List<PagamentoResponse>> listarPagamentos() {
        List<PagamentoResponse> lista = applicationService.pagamentos().stream()
                .map(PagamentoResponse::de)
                .toList();
        return ResponseEntity.ok(lista);
    }

    public record ParcelaRequest(String referencia, double valor) {}

    public record PagamentoRequest(String referencia, double valor, String forma) {}

    public record ConfirmarRequest(String referencia, double valor) {}

    public record CancelarRequest(String referencia, String justificativa) {}

    /** DTO de leitura de uma parcela pagável. */
    public record ParcelaResponse(String referencia, double valorDevido, double valorPago,
                                  double saldoRemanescente, String status) {
        static ParcelaResponse de(ParcelaPagavel p) {
            return new ParcelaResponse(p.getReferencia(), p.getValorDevido(), p.getValorPago(),
                    p.getSaldoRemanescente(), p.getStatus().name());
        }
    }

    /** DTO de leitura de um pagamento (histórico). */
    public record PagamentoResponse(long id, String referencia, double valorRecebido, String dataPagamento,
                                    String formaPagamento, String status, String observacao,
                                    String justificativaCancelamento) {
        static PagamentoResponse de(Pagamento p) {
            return new PagamentoResponse(
                    p.getId().id(), p.getParcelaReferencia(), p.getValorRecebido(),
                    p.getDataPagamento() == null ? null : p.getDataPagamento().toString(),
                    p.getFormaPagamento().name(), p.getStatus().name(),
                    p.getObservacao(), p.getJustificativaCancelamento());
        }
    }
}
