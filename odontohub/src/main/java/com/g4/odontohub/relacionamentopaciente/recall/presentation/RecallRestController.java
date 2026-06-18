package com.g4.odontohub.relacionamentopaciente.recall.presentation;

import com.g4.odontohub.relacionamentopaciente.recall.application.RecallApplicationService;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.CanalContato;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.CategoriaRecall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.FatoresPriorizacao;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.IndicadorVisual;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.MotivoExclusao;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.Recall;
import com.g4.odontohub.relacionamentopaciente.recall.domain.model.ResultadoContato;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Camada de apresentação (REST) do contexto de Recall — F07. */
@RestController
@RequestMapping("/api/recalls")
public class RecallRestController {

    private final RecallApplicationService applicationService;

    public RecallRestController(RecallApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/gatilho")
    public ResponseEntity<Recall> dispararGatilho(@RequestBody RecallRequest request) {
        return ResponseEntity.ok(
                applicationService.processarGatilhoRecall(request.paciente(), request.procedimento()));
    }

    /** Lista todos os recalls (leitura para o frontend). */
    @GetMapping
    public ResponseEntity<List<Recall>> listar() {
        return ResponseEntity.ok(applicationService.todos());
    }

    @GetMapping("/paciente/{paciente}")
    public ResponseEntity<Recall> consultar(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.buscarPorPaciente(paciente));
    }

    /** Fila de recall ordenada pela pontuação do motor de priorização (maior primeiro). */
    @GetMapping("/fila-priorizada")
    public ResponseEntity<List<Recall>> filaPriorizada() {
        return ResponseEntity.ok(applicationService.filaPriorizada());
    }

    /** Registra uma tentativa de contato simples (SLA); retorna se o caso foi escalonado. */
    @PostMapping("/paciente/{paciente}/tentativa-contato")
    public ResponseEntity<Boolean> registrarTentativaContato(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.registrarTentativaContato(paciente));
    }

    /** Define os fatores do motor de priorização e reclassifica a categoria do recall. */
    @PostMapping("/paciente/{paciente}/fatores")
    public ResponseEntity<Recall> definirFatores(@PathVariable String paciente,
                                                 @RequestBody FatoresRequest r) {
        FatoresPriorizacao fatores = new FatoresPriorizacao(
                r.mesesSemRetorno(), r.tratamentosPendentes(), r.procedimentoCritico(), r.inadimplente(),
                r.historicoFaltas(), r.cancelamentosRecorrentes(), r.riscoClinicoAlto(), r.potencialFinanceiro(),
                r.tratamentoInterrompido(), r.pacienteEvadido());
        return ResponseEntity.ok(applicationService.definirFatores(paciente, fatores));
    }

    /** Registra uma tentativa de contato detalhada (canal, duração, resultado, responsável). */
    @PostMapping("/paciente/{paciente}/tentativa-detalhada")
    public ResponseEntity<Boolean> registrarTentativaDetalhada(@PathVariable String paciente,
                                                               @RequestBody TentativaRequest r) {
        boolean escalonado = applicationService.registrarTentativaDetalhada(paciente,
                CanalContato.fromLabel(r.canal()), ResultadoContato.fromLabel(r.resultado()),
                r.responsavel(), r.duracaoMinutos(), r.observacoes());
        return ResponseEntity.ok(escalonado);
    }

    /** Exclui o paciente do recall automaticamente (falecido, transferido, alta, judicial). */
    @PostMapping("/paciente/{paciente}/excluir")
    public ResponseEntity<Recall> excluir(@PathVariable String paciente, @RequestBody ExclusaoRequest r) {
        return ResponseEntity.ok(
                applicationService.excluirAutomaticamente(paciente, MotivoExclusao.fromLabel(r.motivo())));
    }

    /** Casos críticos na fila cujo SLA de contato foi ultrapassado. */
    @GetMapping("/sla-vencido")
    public ResponseEntity<List<Recall>> slaVencido(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hoje) {
        return ResponseEntity.ok(applicationService.filaComSlaVencido(hoje));
    }

    @GetMapping("/paciente/{paciente}/categoria")
    public ResponseEntity<CategoriaRecall> categoria(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.categoriaDe(paciente));
    }

    @GetMapping("/paciente/{paciente}/indicador")
    public ResponseEntity<IndicadorVisual> indicador(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.indicadorDe(paciente));
    }

    @GetMapping("/paciente/{paciente}/pontuacao")
    public ResponseEntity<Integer> pontuacao(@PathVariable String paciente) {
        return ResponseEntity.ok(applicationService.pontuacaoDe(paciente));
    }

    /** Taxa de conversão da fila de recall (convertidos / disparados). */
    @GetMapping("/metricas/taxa-conversao")
    public ResponseEntity<Double> taxaConversao() {
        return ResponseEntity.ok(applicationService.taxaConversao());
    }

    @GetMapping("/metricas/pacientes-recuperados")
    public ResponseEntity<Long> pacientesRecuperados() {
        return ResponseEntity.ok(applicationService.pacientesRecuperados());
    }

    @GetMapping("/metricas/pacientes-perdidos")
    public ResponseEntity<Long> pacientesPerdidos() {
        return ResponseEntity.ok(applicationService.pacientesPerdidos());
    }

    public record RecallRequest(String paciente, String procedimento) {}

    public record FatoresRequest(int mesesSemRetorno, int tratamentosPendentes, boolean procedimentoCritico,
                                 boolean inadimplente, int historicoFaltas, int cancelamentosRecorrentes,
                                 boolean riscoClinicoAlto, double potencialFinanceiro,
                                 boolean tratamentoInterrompido, boolean pacienteEvadido) {}

    public record TentativaRequest(String canal, String resultado, String responsavel,
                                   int duracaoMinutos, String observacoes) {}

    public record ExclusaoRequest(String motivo) {}
}
