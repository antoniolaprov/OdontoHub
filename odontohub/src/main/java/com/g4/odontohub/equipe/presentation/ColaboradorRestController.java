package com.g4.odontohub.equipe.presentation;

import com.g4.odontohub.equipe.application.ColaboradorApplicationService;
import com.g4.odontohub.equipe.domain.model.AlteracaoCadastral;
import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.model.DiaSemana;
import com.g4.odontohub.equipe.domain.model.IndicadoresDesempenho;
import com.g4.odontohub.equipe.domain.model.RegistroAuditoria;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Camada de apresentação (REST) do contexto de Equipe — F12. */
@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorRestController {

    private final ColaboradorApplicationService applicationService;

    public ColaboradorRestController(ColaboradorApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Colaborador> cadastrar(@RequestBody ColaboradorRequest request) {
        Colaborador c = applicationService.cadastrar(
                request.nomeCompleto(), request.cpf(), request.telefone(), request.funcao());
        return ResponseEntity.ok(c);
    }

    /** Cadastro completo com credenciais (login/senha) e e-mail. */
    @PostMapping("/completo")
    public ResponseEntity<Colaborador> cadastrarCompleto(@RequestBody ColaboradorCompletoRequest request) {
        Colaborador c = applicationService.cadastrarCompleto(
                request.nomeCompleto(), request.cpf(), request.telefone(),
                request.email(), request.login(), request.senha(), request.funcao());
        return ResponseEntity.ok(c);
    }

    @PostMapping("/{nome}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable String nome) {
        applicationService.desativar(nome);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{nome}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable String nome) {
        applicationService.reativar(nome);
        return ResponseEntity.noContent().build();
    }

    /** Altera o status operacional (Ativo, Inativo, Suspenso, Férias, Afastado). */
    @PostMapping("/{nome}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable String nome, @RequestBody StatusRequest request) {
        applicationService.alterarStatus(nome, request.status());
        return ResponseEntity.noContent().build();
    }

    /** Tenta autenticar; bloqueia a conta após tentativas inválidas. */
    @PostMapping("/{nome}/login")
    public ResponseEntity<Boolean> login(@PathVariable String nome, @RequestBody LoginRequest request) {
        return ResponseEntity.ok(applicationService.tentarLogin(nome, request.senha()));
    }

    @GetMapping("/responsaveis-esterilizacao")
    public ResponseEntity<List<Colaborador>> responsaveisEsterilizacao() {
        return ResponseEntity.ok(applicationService.listarResponsaveisEsterilizacao());
    }

    // ----- Disponibilidade -----

    /** Define dias e horário de trabalho do colaborador. */
    @PostMapping("/{nome}/disponibilidade")
    public ResponseEntity<Void> definirDisponibilidade(@PathVariable String nome,
                                                       @RequestBody DisponibilidadeRequest r) {
        Set<DiaSemana> dias = new LinkedHashSet<>();
        for (String dia : r.dias()) {
            dias.add(DiaSemana.fromLabel(dia));
        }
        applicationService.definirDisponibilidade(nome, dias, r.horaInicio(), r.horaFim());
        return ResponseEntity.noContent().build();
    }

    /** Registra um período de ausência (férias, afastamento, bloqueio temporário). */
    @PostMapping("/{nome}/ausencia")
    public ResponseEntity<Void> registrarAusencia(@PathVariable String nome, @RequestBody AusenciaRequest r) {
        applicationService.registrarAusencia(nome,
                LocalDate.parse(r.inicio()), LocalDate.parse(r.fim()), r.tipo(), r.motivo());
        return ResponseEntity.noContent().build();
    }

    /** Verifica se o colaborador está disponível para agendamento no momento informado. */
    @GetMapping("/{nome}/disponivel")
    public ResponseEntity<Boolean> disponivelEm(@PathVariable String nome,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime momento) {
        return ResponseEntity.ok(applicationService.estaDisponivelEm(nome, momento));
    }

    // ----- Auditoria e rastreabilidade -----

    @PostMapping("/{nome}/acao")
    public ResponseEntity<Void> registrarAcao(@PathVariable String nome, @RequestBody AcaoRequest r) {
        applicationService.registrarAcao(nome, r.modulo(), r.acao());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{nome}/auditoria")
    public ResponseEntity<List<RegistroAuditoria>> auditoria(@PathVariable String nome) {
        return ResponseEntity.ok(applicationService.auditoriaDe(nome));
    }

    @GetMapping("/{nome}/rastreabilidade")
    public ResponseEntity<List<RegistroAuditoria>> rastreabilidade(@PathVariable String nome,
                                                                   @RequestParam String modulo) {
        return ResponseEntity.ok(applicationService.rastrearAcoes(nome, modulo));
    }

    // ----- Histórico de alterações -----

    @PutMapping("/{nome}/dados")
    public ResponseEntity<Void> alterarDado(@PathVariable String nome, @RequestBody AlteracaoRequest r) {
        applicationService.alterarDado(nome, r.campo(), r.novoValor(), r.responsavel());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{nome}/historico-alteracoes")
    public ResponseEntity<List<AlteracaoCadastral>> historicoAlteracoes(@PathVariable String nome) {
        return ResponseEntity.ok(applicationService.historicoAlteracoesDe(nome));
    }

    // ----- Indicadores de desempenho -----

    @PostMapping("/{nome}/atendimento")
    public ResponseEntity<Void> registrarAtendimento(@PathVariable String nome) {
        applicationService.registrarAtendimento(nome);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{nome}/falta")
    public ResponseEntity<Void> registrarFalta(@PathVariable String nome) {
        applicationService.registrarFalta(nome);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{nome}/conversao")
    public ResponseEntity<Void> registrarConversao(@PathVariable String nome) {
        applicationService.registrarConversao(nome);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{nome}/indicadores")
    public ResponseEntity<IndicadoresDesempenho> indicadores(@PathVariable String nome) {
        return ResponseEntity.ok(applicationService.indicadoresDe(nome));
    }

    public record ColaboradorRequest(String nomeCompleto, String cpf, String telefone, String funcao) {}

    public record ColaboradorCompletoRequest(String nomeCompleto, String cpf, String telefone,
                                             String email, String login, String senha, String funcao) {}

    public record StatusRequest(String status) {}

    public record LoginRequest(String senha) {}

    public record DisponibilidadeRequest(List<String> dias, int horaInicio, int horaFim) {}

    public record AusenciaRequest(String inicio, String fim, String tipo, String motivo) {}

    public record AcaoRequest(String modulo, String acao) {}

    public record AlteracaoRequest(String campo, String novoValor, String responsavel) {}
}
