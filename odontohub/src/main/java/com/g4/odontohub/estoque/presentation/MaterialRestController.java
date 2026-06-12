package com.g4.odontohub.estoque.presentation;

import com.g4.odontohub.estoque.application.MaterialApplicationService;
import com.g4.odontohub.estoque.domain.model.MaterialConsumivel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Camada de apresentação (REST) de materiais consumíveis — F05. */
@RestController
@RequestMapping("/api/materiais")
public class MaterialRestController {

    private final MaterialApplicationService applicationService;

    public MaterialRestController(MaterialApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<MaterialConsumivel> cadastrar(@RequestBody MaterialRequest r) {
        return ResponseEntity.ok(applicationService.cadastrarMaterial(
                r.nome(), r.unidadeMedida(), r.saldoInicial(), r.pontoMinimo()));
    }

    @PostMapping("/{nome}/reposicao")
    public ResponseEntity<MaterialConsumivel> repor(@PathVariable String nome, @RequestBody ReposicaoRequest r) {
        applicationService.registrarReposicao(nome, r.fornecedor(), r.quantidade(), r.custoUnitario());
        return ResponseEntity.ok(applicationService.buscarPorNome(nome));
    }

    @GetMapping("/{nome}")
    public ResponseEntity<MaterialConsumivel> consultar(@PathVariable String nome) {
        return ResponseEntity.ok(applicationService.buscarPorNome(nome));
    }

    public record MaterialRequest(String nome, String unidadeMedida, int saldoInicial, int pontoMinimo) {}

    public record ReposicaoRequest(String fornecedor, int quantidade, double custoUnitario) {}
}
