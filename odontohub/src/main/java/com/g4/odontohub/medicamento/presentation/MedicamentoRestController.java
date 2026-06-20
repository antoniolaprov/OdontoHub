package com.g4.odontohub.medicamento.presentation;

import com.g4.odontohub.medicamento.application.MedicamentoApplicationService;
import com.g4.odontohub.medicamento.domain.model.Medicamento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Camada de apresentação (REST) do catálogo de medicamentos — F8. */
@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoRestController {

    private final MedicamentoApplicationService applicationService;

    public MedicamentoRestController(MedicamentoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Medicamento> cadastrar(@RequestBody MedicamentoRequest request) {
        Medicamento m = applicationService.cadastrar(
                request.nomeComercial(), request.principioAtivo(),
                request.categoriaTerapeutica(), request.classeFarmacologica());
        return ResponseEntity.ok(m);
    }

    @PostMapping("/{nome}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable String nome, @RequestParam String justificativa) {
        applicationService.inativar(nome, justificativa);
        return ResponseEntity.noContent().build();
    }

    /** Lista o catálogo de medicamentos (leitura para o frontend). */
    @GetMapping
    public ResponseEntity<List<Medicamento>> listar() {
        return ResponseEntity.ok(applicationService.listarParaSelecao());
    }

    @GetMapping("/selecao")
    public ResponseEntity<List<Medicamento>> listarParaSelecao() {
        return ResponseEntity.ok(applicationService.listarParaSelecao());
    }

    @GetMapping("/{nome}/contraindicacao")
    public ResponseEntity<Boolean> contraindicacao(@PathVariable String nome, @RequestParam String alergia) {
        return ResponseEntity.ok(applicationService.haContraindicacaoCruzada(nome, alergia));
    }

    public record MedicamentoRequest(String nomeComercial, String principioAtivo,
                                     String categoriaTerapeutica, String classeFarmacologica) {}
}
