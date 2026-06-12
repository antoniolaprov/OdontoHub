package com.g4.odontohub.estoque.presentation;

import com.g4.odontohub.estoque.application.InstrumentoApplicationService;
import com.g4.odontohub.estoque.domain.model.Instrumento;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Camada de apresentação (REST) de instrumentos e esterilização — F06/F16. */
@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoRestController {

    private final InstrumentoApplicationService applicationService;

    public InstrumentoRestController(InstrumentoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Instrumento> cadastrar(@RequestBody InstrumentoRequest r) {
        return ResponseEntity.ok(applicationService.cadastrarInstrumentoComPrazo(
                r.nome(), r.categoria(), r.codigoIdentificador(), r.prazoValidadeDias()));
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

    public record InstrumentoRequest(String nome, String categoria,
                                     String codigoIdentificador, int prazoValidadeDias) {}
}
