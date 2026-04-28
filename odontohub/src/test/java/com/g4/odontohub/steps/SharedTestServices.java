package com.g4.odontohub.steps;

import com.g4.odontohub.estoque.application.MaterialApplicationService;
import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.shared.DomainEventPublisher;

public class SharedTestServices {

    private static FluxoCaixaApplicationService fluxoCaixaService;
    private static MaterialApplicationService materialService;

    public static void initialize() {
        DomainEventPublisher.reset();
        fluxoCaixaService = new FluxoCaixaApplicationService();
        materialService = new MaterialApplicationService(fluxoCaixaService);
    }

    public static FluxoCaixaApplicationService getFluxoCaixaService() {
        if (fluxoCaixaService == null) initialize();
        return fluxoCaixaService;
    }

    public static MaterialApplicationService getMaterialService() {
        if (materialService == null) initialize();
        return materialService;
    }
}
