package com.g4.odontohub.relacionamentopaciente.churn.presentation;

import com.g4.odontohub.relacionamentopaciente.churn.application.ChurnApplicationService;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.ItemPareto;
import com.g4.odontohub.relacionamentopaciente.churn.domain.model.StatusChurn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) do contexto de Churn — F11. */
@RestController
@RequestMapping("/api/churn")
public class ChurnRestController {

    private final ChurnApplicationService applicationService;

    public ChurnRestController(ChurnApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** Alimenta os dados de churn do paciente (agendamento futuro, meses sem retorno, plano ativo). */
    @PostMapping("/paciente/{paciente}/dados")
    public ResponseEntity<Void> definirDados(@PathVariable String paciente, @RequestBody DadosPacienteRequest r) {
        if (!r.agendamentoFuturo()) {
            applicationService.definirSemAgendamentoFuturo(paciente);
        }
        applicationService.definirMesesSemRetorno(paciente, r.mesesSemRetorno());
        applicationService.definirPlanoAtivo(paciente, r.planoAtivo());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recalcular")
    public ResponseEntity<Void> recalcular() {
        applicationService.recalcular();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/paciente/{paciente}/status")
    public ResponseEntity<StatusChurn> status(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.statusDe(paciente));
    }

    @GetMapping("/receita-perdida")
    public ResponseEntity<Double> receitaPerdida(@RequestParam int horas) {
        return ResponseEntity.ok(applicationService.calcularReceitaPerdida(horas));
    }

    /** Registra um cancelamento com a categoria de motivo obrigatória (alimenta o Pareto). */
    @PostMapping("/cancelamentos")
    public ResponseEntity<Void> registrarCancelamento(@RequestBody CancelamentoRequest r) {
        applicationService.cancelarAgendamento(r.paciente(), r.categoriaMotivo());
        return ResponseEntity.accepted().build();
    }

    /** Gráfico de Pareto dos motivos de cancelamento (ordenado, com percentual acumulado). */
    @GetMapping("/pareto-cancelamentos")
    public ResponseEntity<List<ItemPareto>> paretoCancelamentos() {
        return ResponseEntity.ok(applicationService.paretoMotivosCancelamento());
    }

    /** Define o tipo de procedimento do tratamento do paciente (para o filtro de churn). */
    @PostMapping("/paciente/{paciente}/tipo-procedimento")
    public ResponseEntity<Void> definirTipoProcedimento(@PathVariable String paciente,
                                                        @RequestBody TipoProcedimentoRequest r) {
        applicationService.definirTipoProcedimento(paciente, r.tipoProcedimento());
        return ResponseEntity.noContent().build();
    }

    /** Filtro de churn por tipo de procedimento: quantos evadidos têm aquele tratamento. */
    @GetMapping("/por-procedimento")
    public ResponseEntity<Long> churnPorProcedimento(@RequestParam String tipo) {
        return ResponseEntity.ok(applicationService.contarChurnPorProcedimento(tipo));
    }

    public record DadosPacienteRequest(String paciente, boolean agendamentoFuturo,
                                       int mesesSemRetorno, boolean planoAtivo) {}

    public record CancelamentoRequest(String paciente, String categoriaMotivo) {}

    public record TipoProcedimentoRequest(String tipoProcedimento) {}
}
