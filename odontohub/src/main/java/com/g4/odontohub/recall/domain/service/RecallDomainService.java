package com.g4.odontohub.recall.domain.service;

import com.g4.odontohub.recall.domain.event.RecallCancelado;
import com.g4.odontohub.recall.domain.event.RecallConvertido;
import com.g4.odontohub.recall.domain.event.RecallDisparado;
import com.g4.odontohub.recall.domain.model.Agendamento;
import com.g4.odontohub.recall.domain.model.FilaDeRecall;
import com.g4.odontohub.recall.domain.model.ProcedimentoRecall;
import com.g4.odontohub.recall.domain.model.StatusRecall;

public class RecallDomainService {

    public RecallDisparado dispararRecall(String nomePaciente, String nomeProcedimento) {
        ProcedimentoRecall procedimento = ProcedimentoRecall.fromNome(nomeProcedimento);
        return new RecallDisparado(nomePaciente, nomeProcedimento, procedimento.getPrazoEmDias());
    }

    public RecallCancelado verificarSobreposicao(FilaDeRecall filaDeRecall, Agendamento agendamento) {
        if (agendamento.isFuturoConfirmado()) {
            filaDeRecall.cancelar(agendamento.getId());
            return new RecallCancelado(filaDeRecall.getNomePaciente(), agendamento.getId());
        }
        return null;
    }

    public RecallConvertido converterRecall(FilaDeRecall filaDeRecall, Agendamento novoAgendamento) {
        novoAgendamento.marcarComoConversaoRecall();
        filaDeRecall.converter();
        return new RecallConvertido(filaDeRecall.getNomePaciente(), novoAgendamento.getId());
    }
}