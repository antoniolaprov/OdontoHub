package com.g4.odontohub.inadimplencia.application;

import com.g4.odontohub.inadimplencia.domain.event.EncaminhamentoPagamentoEvent;
import com.g4.odontohub.inadimplencia.domain.model.*;
import com.g4.odontohub.inadimplencia.domain.service.InadimplenciaDomainService;

import java.util.List;
import java.util.Optional;

public class InadimplenciaApplicationService {

    private final InadimplenciaDomainService domainService;

    public InadimplenciaApplicationService(InadimplenciaDomainService domainService) {
        this.domainService = domainService;
    }

    public List<ResumoInadimplencia> consultarInadimplentes() {
        return domainService.consultarInadimplentes();
    }

    public void atualizarSituacaoFinanceira(String pacienteId) {
        domainService.atualizarSituacaoFinanceira(pacienteId);
    }

    public void quitarUltimaPendencia(String pacienteId) {
        domainService.quitarUltimaPendencia(pacienteId);
    }

    public void validarAgendamento(String pacienteId) {
        domainService.validarAgendamento(pacienteId);
    }

    public TentativaCobranca registrarTentativaCobranca(String pacienteId, String responsavel,
                                                         CanalCobranca canal,
                                                         String resultado, String observacao) {
        return domainService.registrarTentativaCobranca(pacienteId, responsavel, canal, resultado, observacao);
    }

    public Acordo criarAcordo(String pacienteId, int numeroParcelas, String justificativa) {
        return domainService.criarAcordo(pacienteId, numeroParcelas, justificativa);
    }

    public void cancelarAcordo(String acordoId, String responsavel, String justificativa) {
        domainService.cancelarAcordo(acordoId, responsavel, justificativa);
    }

    public void alterarStatusAcordo(String acordoId, StatusAcordo novoStatus,
                                     String responsavel, String justificativa) {
        domainService.alterarStatusAcordo(acordoId, novoStatus, responsavel, justificativa);
    }

    public EncaminhamentoPagamentoEvent encaminharParaPagamento(String parcelaId) {
        return domainService.encaminharParaPagamento(parcelaId);
    }

    // Acesso direto ao domainService para os Steps
    public InadimplenciaDomainService getDomainService() {
        return domainService;
    }
}
