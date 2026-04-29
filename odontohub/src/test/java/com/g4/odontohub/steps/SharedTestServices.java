package com.g4.odontohub.steps;

import com.g4.odontohub.estoque.application.MaterialApplicationService;
import com.g4.odontohub.financeiro.application.FluxoCaixaApplicationService;
import com.g4.odontohub.relacionamentopaciente.application.ChurnApplicationService;
import com.g4.odontohub.shared.DomainEventPublisher;

public class SharedTestServices {

    private static FluxoCaixaApplicationService fluxoCaixaService;
    private static MaterialApplicationService materialService;
    private static ChurnApplicationService churnApplicationService;
    private static Exception lastException;

    public static void initialize() {
        DomainEventPublisher.reset();
        fluxoCaixaService = new FluxoCaixaApplicationService();
        materialService = new MaterialApplicationService(fluxoCaixaService);
        churnApplicationService = new ChurnApplicationService();
        lastException = null;
    }

    public static FluxoCaixaApplicationService getFluxoCaixaService() {
        if (fluxoCaixaService == null) initialize();
        return fluxoCaixaService;
    }

    public static MaterialApplicationService getMaterialService() {
        if (materialService == null) initialize();
        return materialService;
    }

    public static ChurnApplicationService getChurnApplicationService() {
        if (churnApplicationService == null) initialize();
        return churnApplicationService;
    }

    public static void setLastException(Exception e) {
        lastException = e;
    }

    public static Exception getLastException() {
        return lastException;
    }
}
