package com.g4.odontohub.prontuarioclinico.presentation;

import com.g4.odontohub.prontuarioclinico.application.ProntuarioApplicationService;
import com.g4.odontohub.prontuarioclinico.domain.model.Anamnese;
import com.g4.odontohub.prontuarioclinico.domain.model.PlanoTratamento;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.PlanoId;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.ProcedimentoId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Camada de apresentação (REST) do prontuário clínico — F02/F03. */
@RestController
@RequestMapping("/api/prontuarios")
public class ProntuarioRestController {

    private final ProntuarioApplicationService applicationService;

    public ProntuarioRestController(ProntuarioApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/anamnese")
    public ResponseEntity<Void> registrarAnamnese(@RequestBody AnamneseRequest r) {
        applicationService.getAnamneseService().registrarAnamnese(
                r.pacienteId(), r.alergias(), r.contraindicacoes(), r.condicoesSistemicas(), r.responsavel());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/anamnese/{pacienteId}/existe")
    public ResponseEntity<Boolean> anamneseExiste(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(applicationService.getAnamneseService().anamneseExiste(pacienteId));
    }

    @GetMapping("/anamnese/{pacienteId}")
    public ResponseEntity<Anamnese> buscarAnamnese(@PathVariable Long pacienteId) {
        Anamnese anamnese = applicationService.getAnamneseService().buscarPorPacienteId(pacienteId);
        return anamnese == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(anamnese);
    }

    @PostMapping("/anamnese/{pacienteId}/alergias")
    public ResponseEntity<Void> adicionarAlergia(@PathVariable Long pacienteId, @RequestBody AlergiaRequest r) {
        applicationService.getAnamneseService().adicionarAlergia(pacienteId, r.alergia(), r.responsavel());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/anamnese/{pacienteId}/condicoes-sistemicas")
    public ResponseEntity<Void> atualizarCondicoesSistemicas(@PathVariable Long pacienteId,
                                                             @RequestBody CondicoesSistemicasRequest r) {
        applicationService.getAnamneseService().atualizarCondicoesSistemicas(pacienteId, r.condicoes(), r.responsavel());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/planos")
    public ResponseEntity<Void> criarPlano(@RequestBody PlanoRequest r) {
        applicationService.getPlanoTratamentoService().criarPlano(r.pacienteId(), r.dentistaId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/planos/paciente/{pacienteId}")
    public ResponseEntity<PlanoTratamento> buscarPlanoPorPaciente(@PathVariable Long pacienteId) {
        PlanoTratamento plano = applicationService.getPlanoTratamentoService().buscarPorPacienteId(pacienteId);
        return plano == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(plano);
    }

    @PostMapping("/planos/{planoId}/procedimentos")
    public ResponseEntity<Void> adicionarProcedimento(@PathVariable Long planoId, @RequestBody ProcedimentoRequest r) {
        applicationService.getPlanoTratamentoService().adicionarProcedimento(
                new PlanoId(planoId), r.nome(), r.tipoProcedimento());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/planos/{planoId}/procedimentos/{procedimentoId}/realizar")
    public ResponseEntity<Void> realizarProcedimento(@PathVariable Long planoId, @PathVariable Long procedimentoId,
                                                      @RequestBody RealizarProcedimentoRequest r) {
        applicationService.getPlanoTratamentoService().realizarProcedimento(
                new PlanoId(planoId), new ProcedimentoId(procedimentoId), r.descricaoEvolucao(), r.agendamentoId(), r.executor());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/planos/{planoId}/procedimentos/{procedimentoId}/cancelar")
    public ResponseEntity<Void> cancelarProcedimento(@PathVariable Long planoId, @PathVariable Long procedimentoId,
                                                      @RequestBody JustificativaRequest r) {
        applicationService.getPlanoTratamentoService().cancelarProcedimento(
                new PlanoId(planoId), new ProcedimentoId(procedimentoId), r.justificativa());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/planos/{planoId}/procedimentos/{procedimentoId}")
    public ResponseEntity<Void> excluirProcedimento(@PathVariable Long planoId, @PathVariable Long procedimentoId,
                                                     @RequestParam String justificativa, @RequestParam String responsavel) {
        applicationService.getPlanoTratamentoService().excluirProcedimento(
                new PlanoId(planoId), new ProcedimentoId(procedimentoId), justificativa, responsavel);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/planos/{planoId}/encerrar")
    public ResponseEntity<Void> encerrarPlano(@PathVariable Long planoId, @RequestBody JustificativaRequest r) {
        applicationService.getPlanoTratamentoService().encerrarPlano(new PlanoId(planoId), r.justificativa());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/planos/{planoId}")
    public ResponseEntity<Void> excluirPlano(@PathVariable Long planoId) {
        applicationService.getPlanoTratamentoService().excluirPlano(new PlanoId(planoId));
        return ResponseEntity.noContent().build();
    }

    /** Regras de negócio violadas (ex.: anamnese ausente, prazo de correção expirado). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> tratarConflito(IllegalStateException e) {
        return ResponseEntity.status(409).body(e.getMessage());
    }

    /** Entidade referenciada não encontrada (ex.: procedimento inexistente). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarNaoEncontrado(IllegalArgumentException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    public record AnamneseRequest(Long pacienteId, String alergias, String contraindicacoes,
                                  String condicoesSistemicas, String responsavel) {}

    public record AlergiaRequest(String alergia, String responsavel) {}

    public record CondicoesSistemicasRequest(String condicoes, String responsavel) {}

    public record PlanoRequest(Long pacienteId, Long dentistaId) {}

    public record ProcedimentoRequest(String nome, String tipoProcedimento) {}

    public record RealizarProcedimentoRequest(String descricaoEvolucao, Long agendamentoId, String executor) {}

    public record JustificativaRequest(String justificativa) {}
}
