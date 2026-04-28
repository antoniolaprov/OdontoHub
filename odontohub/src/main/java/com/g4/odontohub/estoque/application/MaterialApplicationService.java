package com.g4.odontohub.estoque.application;

import com.g4.odontohub.estoque.domain.event.EstoqueBaixoAlertado;
import com.g4.odontohub.estoque.domain.event.ReposicaoRegistrada;
import com.g4.odontohub.estoque.domain.model.MaterialConsumivel;
import com.g4.odontohub.estoque.domain.service.MaterialService;
import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.shared.DomainEventPublisher;

import java.util.List;

public class MaterialApplicationService {

    private final MaterialService materialService = new MaterialService();
    private final FluxoCaixaApplicationService fluxoCaixaService;
    private EstoqueBaixoAlertado ultimoAlertaEstoque;

    public MaterialApplicationService(FluxoCaixaApplicationService fluxoCaixaService) {
        this.fluxoCaixaService = fluxoCaixaService;
        DomainEventPublisher.subscribe(ReposicaoRegistrada.class, evento ->
                this.fluxoCaixaService.registrarSaidaAutomatica(
                        evento.valorTotalLancamento(), "Insumos", "Reposição de material"));
    }

    public MaterialConsumivel cadastrarMaterial(String nome, String unidadeMedida,
                                                int saldoInicial, int pontoMinimo) {
        return materialService.cadastrarMaterial(nome, unidadeMedida, saldoInicial, pontoMinimo);
    }

    public MaterialConsumivel buscarPorNome(String nome) {
        return materialService.buscarPorNome(nome);
    }

    public void registrarReposicao(String materialNome, String fornecedor,
                                   int quantidade, double custoUnitario) {
        List<Object> eventos = materialService.registrarReposicao(
                materialNome, fornecedor, quantidade, custoUnitario);
        eventos.forEach(DomainEventPublisher::publish);
    }

    public void descontarConsumo(String materialNome, int quantidade, Long procedimentoId) {
        List<Object> eventos = materialService.descontarConsumo(materialNome, quantidade, procedimentoId);
        for (Object evento : eventos) {
            if (evento instanceof EstoqueBaixoAlertado alerta) {
                ultimoAlertaEstoque = alerta;
            }
            DomainEventPublisher.publish(evento);
        }
    }

    public void ajustarSaldo(String materialNome, int novaQuantidade) {
        materialService.ajustarSaldo(materialNome, novaQuantidade);
    }

    public EstoqueBaixoAlertado getUltimoAlertaEstoque() { return ultimoAlertaEstoque; }
}
