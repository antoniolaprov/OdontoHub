package com.g4.odontohub.estoque.presentation;

import com.g4.odontohub.estoque.application.InstrumentoApplicationService;
import com.g4.odontohub.estoque.domain.model.Instrumento;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/** Camada de apresentação (REST) de instrumentos e esterilização — F06/F14. */
@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoRestController {

    private final InstrumentoApplicationService applicationService;

    public InstrumentoRestController(InstrumentoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Instrumento> cadastrar(@RequestBody InstrumentoRequest r) {
        Instrumento instrumento = "KIT".equalsIgnoreCase(r.tipo())
                ? applicationService.cadastrarKit(r.nome(), r.categoria(), r.codigoIdentificador(),
                        r.prazoValidadeDias(), r.codigosComponentes() == null ? Collections.emptyList() : r.codigosComponentes())
                : applicationService.cadastrarInstrumentoComPrazo(
                        r.nome(), r.categoria(), r.codigoIdentificador(), r.prazoValidadeDias());
        return ResponseEntity.ok(instrumento);
    }

    @PostMapping("/prazo-global")
    public ResponseEntity<Void> configurarPrazoGlobal(@RequestParam int dias) {
        applicationService.configurarPrazoGlobal(dias);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categorias/{categoria}/prazo")
    public ResponseEntity<Void> configurarPrazoPorCategoria(@PathVariable String categoria, @RequestParam int dias) {
        applicationService.configurarPrazoPorCategoria(categoria, dias);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{nome}/esterilizar")
    public ResponseEntity<Instrumento> esterilizar(@PathVariable String nome, @RequestParam String responsavel,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        applicationService.marcarComoEsteril(nome, data == null ? LocalDate.now() : data, responsavel);
        return ResponseEntity.ok(applicationService.buscarPorNome(nome));
    }

    @GetMapping("/prontos")
    public ResponseEntity<List<Instrumento>> prontos() {
        return ResponseEntity.ok(applicationService.listarEstereisDentroDoPrazo());
    }

    /** Lista todos os instrumentos (ativos e inativos) — leitura para o frontend (F06/F14). */
    @GetMapping
    public ResponseEntity<List<Instrumento>> listar() {
        return ResponseEntity.ok(applicationService.listarTodos());
    }

    @PostMapping("/{nome}/contaminar")
    public ResponseEntity<Instrumento> contaminar(@PathVariable String nome) {
        applicationService.marcarComoContaminado(nome);
        return ResponseEntity.ok(applicationService.buscarPorNome(nome));
    }

    @PostMapping("/{nome}/desativar")
    public ResponseEntity<Instrumento> desativar(@PathVariable String nome) {
        applicationService.desativarInstrumento(nome);
        return ResponseEntity.ok(applicationService.buscarPorNome(nome));
    }

    public record InstrumentoRequest(String nome, String categoria, String codigoIdentificador,
                                     int prazoValidadeDias, String tipo, List<String> codigosComponentes) {}
}
