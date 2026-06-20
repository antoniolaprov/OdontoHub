package com.g4.odontohub.agendamento.presentation;

import com.g4.odontohub.agendamento.application.AgendamentoApplicationService;
import com.g4.odontohub.agendamento.domain.model.Agendamento;
import com.g4.odontohub.agendamento.domain.model.AgendamentoId;
import com.g4.odontohub.agendamento.domain.model.EntradaHistorico;
import com.g4.odontohub.agendamento.domain.model.StatusAgendamento;
import com.g4.odontohub.agendamento.domain.model.TipoAtendimento;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** Camada de apresentação (REST) do contexto de Agendamento — F01. */
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoRestController {

    private final AgendamentoApplicationService applicationService;

    public AgendamentoRestController(AgendamentoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Agendamento> registrar(@RequestBody AgendamentoRequest r) {
        applicationService.cadastrarPaciente(r.paciente());
        applicationService.cadastrarDentista(r.dentista());
        Agendamento ag = applicationService.registrarAgendamento(r.paciente(), r.dentista(), r.dataHora());
        return ResponseEntity.ok(ag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> consultar(@PathVariable Long id) {
        Agendamento ag = applicationService.buscarPorId(new AgendamentoId(id));
        return ag == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(ag);
    }

    /** Lista todos os agendamentos, com nomes de paciente/dentista resolvidos — leitura para o frontend. */
    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listar() {
        List<AgendamentoResponse> resposta = applicationService.listarTodos().stream()
                .map(this::paraResposta)
                .toList();
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<AgendamentoResponse> confirmar(@PathVariable Long id, @RequestParam String responsavel) {
        applicationService.confirmarAgendamento(new AgendamentoId(id), responsavel);
        return ResponseEntity.ok(paraResposta(applicationService.buscarPorId(new AgendamentoId(id))));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoResponse> cancelar(@PathVariable Long id, @RequestParam String motivo,
                                                        @RequestParam String responsavel) {
        applicationService.cancelarAgendamento(new AgendamentoId(id), motivo, responsavel);
        return ResponseEntity.ok(paraResposta(applicationService.buscarPorId(new AgendamentoId(id))));
    }

    @PostMapping("/{id}/remarcar")
    public ResponseEntity<AgendamentoResponse> remarcar(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime novaDataHora,
            @RequestParam String responsavel) {
        applicationService.remarcarAgendamento(new AgendamentoId(id), novaDataHora, responsavel);
        return ResponseEntity.ok(paraResposta(applicationService.buscarPorId(new AgendamentoId(id))));
    }

    private AgendamentoResponse paraResposta(Agendamento a) {
        return new AgendamentoResponse(
                a.getId().id(),
                applicationService.nomeDoPaciente(a.getPacienteId().id()),
                applicationService.nomeDoDentista(a.getDentistaId().id()),
                a.getDataHora(), a.getTipo(), a.getStatus(), a.getMotivoCancelamento(),
                a.getResponsavelAlteracao(), a.getDataUltimaAlteracao(), a.getHistorico());
    }

    /** Mapeia erros de validação/regra de negócio para 400, em vez do 500 genérico padrão. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarErroDeValidacao(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    public record AgendamentoRequest(String paciente, String dentista,
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHora) {}

    /** DTO de leitura com nomes resolvidos (o agregado de domínio só guarda PacienteId/DentistaId). */
    public record AgendamentoResponse(Long id, String paciente, String dentista, LocalDateTime dataHora,
                                      TipoAtendimento tipo, StatusAgendamento status, String motivoCancelamento,
                                      String responsavelAlteracao, LocalDateTime dataUltimaAlteracao,
                                      List<EntradaHistorico> historico) {}
}
