package com.g4.odontohub.financeiro.application;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.ContatoCobranca;
import com.g4.odontohub.financeiro.domain.model.HistoricoNegociacao;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import com.g4.odontohub.financeiro.domain.repository.InadimplenciaRepository;
import com.g4.odontohub.financeiro.domain.service.InadimplenciaService;
import com.g4.odontohub.financeiro.infrastructure.persistence.InMemoryInadimplenciaRepository;

import java.util.List;

public class InadimplenciaApplicationService {

    private final InadimplenciaService service;

    public InadimplenciaApplicationService() {
        this(new InMemoryInadimplenciaRepository());
    }

    public InadimplenciaApplicationService(InadimplenciaRepository repositorio) {
        this.service = new InadimplenciaService(repositorio);
    }

    public void registrarPaciente(String paciente) {
        service.registrarPaciente(paciente);
    }

    public ParcelaCobranca registrarParcelaVencida(String paciente, double valor, int diasAtraso) {
        return service.registrarParcelaVencida(paciente, valor, diasAtraso);
    }

    public void registrarParcelasVencidas(String paciente, int quantidade, double valorTotal) {
        service.registrarParcelasVencidas(paciente, quantidade, valorTotal);
    }

    public List<ParcelaCobranca> consultarInadimplentes() {
        return service.consultarInadimplentes();
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

    public Acordo firmarAcordo(String paciente, int quantidadeNovas, String justificativa) {
        return service.firmarAcordo(paciente, quantidadeNovas, justificativa);
    }

    public void inadimplirAcordo(String paciente) {
        service.inadimplirAcordo(paciente);
    }

    public void marcarParcelaDoAcordoVencida(String paciente) {
        service.marcarParcelaDoAcordoVencida(paciente);
    }

    public void atualizarSituacaoFinanceira(String paciente) {
        service.atualizarSituacaoFinanceira(paciente);
    }

    public void quitarPendencias(String paciente) {
        service.quitarPendencias(paciente);
    }

    public void registrarContato(String paciente, String canal) {
        service.registrarContato(paciente, canal);
    }

    public boolean semCobrancaRecente(String paciente, int dias) {
        return service.semCobrancaRecente(paciente, dias);
    }

    public void cancelarAcordo(String paciente, String justificativa) {
        service.cancelarAcordo(paciente, justificativa);
    }

    public void alterarStatusAcordo(String paciente) {
        service.alterarStatusAcordo(paciente);
    }

    public Acordo acordoDe(String paciente) {
        return service.acordoDe(paciente);
    }

    public java.util.Set<String> pacientes() {
        return service.pacientes();
    }

    public List<ParcelaCobranca> vencidasDe(String paciente) {
        return service.vencidasDe(paciente);
    }

    public Acordo acordoDeOuNull(String paciente) {
        return service.acordoDeOuNull(paciente);
    }

    public ContatoCobranca ultimoContatoDe(String paciente) {
        return service.ultimoContatoDe(paciente);
    }

    public HistoricoNegociacao ultimoHistoricoDe(String paciente) {
        return service.ultimoHistoricoDe(paciente);
    }

    public String selecionarRegistroPagamento(String paciente) {
        return service.selecionarRegistroPagamento(paciente);
    }

    public double totalSubstituidasNoFluxoCaixa(String paciente) {
        return service.totalSubstituidasNoFluxoCaixa(paciente);
    }
}
