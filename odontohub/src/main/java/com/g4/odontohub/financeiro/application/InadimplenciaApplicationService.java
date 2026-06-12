package com.g4.odontohub.financeiro.application;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.service.InadimplenciaService;

public class InadimplenciaApplicationService {

    private final InadimplenciaService service = new InadimplenciaService();

    public ParcelaCobranca registrarParcelaVencida(String paciente, double valor, int diasAtraso) {
        return service.registrarParcelaVencida(paciente, valor, diasAtraso);
    }

    public void registrarParcelasVencidas(String paciente, int quantidade, double valorTotal) {
        service.registrarParcelasVencidas(paciente, quantidade, valorTotal);
    }

    public boolean verificarRestricao(String paciente) {
        return service.verificarRestricao(paciente);
    }

    public boolean agendamentoBloqueado(String paciente) {
        return service.agendamentoBloqueado(paciente);
    }

    public ParcelaCobranca consultarParcela(String paciente) {
        return service.consultarParcela(paciente);
    }

    public Acordo firmarAcordo(String paciente, int quantidadeNovas, double valorNova) {
        return service.firmarAcordo(paciente, quantidadeNovas, valorNova);
    }

    public void inadimplirAcordo(String paciente) {
        service.inadimplirAcordo(paciente);
    }

    public Acordo acordoDe(String paciente) {
        return service.acordoDe(paciente);
    }

    public double totalSubstituidasNoFluxoCaixa(String paciente) {
        return service.totalSubstituidasNoFluxoCaixa(paciente);
    }
}
