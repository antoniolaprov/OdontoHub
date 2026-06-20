package com.g4.odontohub.financeiro.presentation;

import com.g4.odontohub.financeiro.application.InadimplenciaApplicationService;
import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.model.StatusAcordo;
import com.g4.odontohub.financeiro.domain.model.StatusParcela;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) de inadimplência e acordos — F09. */
@RestController
@RequestMapping("/api/inadimplencia")
public class InadimplenciaRestController {

    private static final int DIAS_COBRANCA_RECENTE = 15;

    private final InadimplenciaApplicationService applicationService;

    public InadimplenciaRestController(InadimplenciaApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** Lista inadimplentes agrupados por paciente (parcelas vencidas + acordo). */
    @GetMapping
    public ResponseEntity<List<InadimplenteResponse>> listar() {
        List<InadimplenteResponse> lista = applicationService.pacientes().stream()
                .map(this::montarInadimplente)
                .filter(i -> !i.parcelas().isEmpty() || i.acordo() != null)
                .sorted((a, b) -> Double.compare(b.totalAtualizado(), a.totalAtualizado()))
                .toList();
        return ResponseEntity.ok(lista);
    }

    private InadimplenteResponse montarInadimplente(String paciente) {
        Acordo acordo = applicationService.acordoDeOuNull(paciente);
        boolean acordoVigente = acordo != null && acordo.getStatus() != StatusAcordo.CANCELADO;

        List<ParcelaResponse> parcelas;
        double total;
        boolean restrito;
        if (acordoVigente) {
            // Com acordo vigente, as parcelas originais foram substituídas: a fonte
            // da verdade passa a ser o acordo (independe do status persistido das
            // originais). Restrição volta apenas se o acordo for inadimplido.
            parcelas = List.of();
            total = totalDoAcordo(acordo);
            restrito = acordo.getStatus() == StatusAcordo.INADIMPLIDO;
        } else {
            parcelas = applicationService.vencidasDe(paciente).stream()
                    .filter(p -> p.getStatus() == StatusParcela.VENCIDA)
                    .map(ParcelaResponse::de)
                    .toList();
            total = parcelas.stream().mapToDouble(ParcelaResponse::valorAtualizado).sum();
            restrito = applicationService.verificarRestricao(paciente);
        }
        return new InadimplenteResponse(
                paciente,
                restrito,
                applicationService.semCobrancaRecente(paciente, DIAS_COBRANCA_RECENTE),
                total,
                parcelas,
                acordo == null ? null : AcordoResponse.de(acordo));
    }

    /**
     * Total do acordo calculado pelas novas parcelas negociadas. Evita o
     * {@code valorTotal} do agregado, que volta como 0 quando o acordo é
     * reconstituído do banco (limitação atual da reconstituição).
     */
    private static double totalDoAcordo(Acordo acordo) {
        return acordo.getNovasParcelas().stream()
                .mapToDouble(ParcelaCobranca::getValorAtualizado)
                .sum();
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

    /** Registra uma tentativa de cobrança (telefone, e-mail, etc.). */
    @PostMapping("/{paciente}/cobranca")
    public ResponseEntity<Void> registrarCobranca(@PathVariable String paciente, @RequestBody CobrancaRequest r) {
        applicationService.registrarContato(paciente, r.canal());
        return ResponseEntity.noContent().build();
    }

    /** Cancela o acordo ativo do paciente (exige justificativa). */
    @PostMapping("/{paciente}/acordo/cancelar")
    public ResponseEntity<Void> cancelarAcordo(@PathVariable String paciente, @RequestBody CancelarAcordoRequest r) {
        applicationService.cancelarAcordo(paciente, r.justificativa());
        return ResponseEntity.noContent().build();
    }

    public record ParcelaVencidaRequest(String paciente, double valor, int diasAtraso) {}

    public record AcordoRequest(int quantidadeNovas, double valorNova) {}

    public record CobrancaRequest(String canal) {}

    public record CancelarAcordoRequest(String justificativa) {}

    /** DTO de leitura de uma parcela vencida. */
    public record ParcelaResponse(double valor, int diasAtraso, double multa, double juros,
                                  double valorAtualizado, String status) {
        static ParcelaResponse de(ParcelaCobranca p) {
            return new ParcelaResponse(p.getValor(), p.getDiasAtraso(), p.getMulta(), p.getJuros(),
                    p.getValorAtualizado(), p.getStatus().name());
        }
    }

    /** DTO de leitura de um acordo. */
    public record AcordoResponse(long id, String status, double valorTotal, int quantidadeParcelas,
                                 String justificativa) {
        static AcordoResponse de(Acordo a) {
            double total = a.getNovasParcelas().stream()
                    .mapToDouble(ParcelaCobranca::getValorAtualizado).sum();
            return new AcordoResponse(a.getId().id(), a.getStatus().name(), total,
                    a.getNovasParcelas().size(), a.getJustificativa());
        }
    }

    /** DTO de leitura de um inadimplente (paciente + suas parcelas + acordo). */
    public record InadimplenteResponse(String paciente, boolean restrito, boolean semCobrancaRecente,
                                       double totalAtualizado, List<ParcelaResponse> parcelas,
                                       AcordoResponse acordo) {}
}
