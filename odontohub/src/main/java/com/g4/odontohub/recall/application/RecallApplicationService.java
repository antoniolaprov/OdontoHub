package com.g4.odontohub.recall.application;

import com.g4.odontohub.recall.domain.event.RecallCancelado;
import com.g4.odontohub.recall.domain.event.RecallConvertido;
import com.g4.odontohub.recall.domain.event.RecallDisparado;
import com.g4.odontohub.recall.domain.model.Agendamento;
import com.g4.odontohub.recall.domain.model.FilaDeRecall;
import com.g4.odontohub.recall.domain.model.Paciente;
import com.g4.odontohub.recall.domain.model.StatusRecall;
import com.g4.odontohub.recall.domain.service.RecallDomainService;

import java.util.HashMap;
import java.util.Map;

public class RecallApplicationService {

    private final RecallDomainService domainService = new RecallDomainService();

    private final Map<String, Paciente> pacientes = new HashMap<>();
    private final Map<String, FilaDeRecall> filaDeRecall = new HashMap<>();
    private final Map<String, Agendamento> agendamentos = new HashMap<>();

    private RecallDisparado ultimoEventoDisparado;
    private RecallCancelado ultimoEventoCancelado;
    private RecallConvertido ultimoEventoConvertido;

    public void cadastrarPaciente(String nome) {
        pacientes.put(nome, new Paciente(nome));
    }

    public void registrarProcedimento(String nomePaciente, String nomeProcedimento) {
        RecallDisparado evento = domainService.dispararRecall(nomePaciente, nomeProcedimento);
        this.ultimoEventoDisparado = evento;
    }

    public void processarGatilhosDeRecall() {
        if (ultimoEventoDisparado != null) {
            String nome = ultimoEventoDisparado.getNomePaciente();
            FilaDeRecall fila = new FilaDeRecall(nome, ultimoEventoDisparado.getPrazoEmDias());
            filaDeRecall.put(nome, fila);
        }
    }

    public void adicionarNaFilaComStatus(String nomePaciente, String status) {
        FilaDeRecall fila = new FilaDeRecall(nomePaciente, 0);
        filaDeRecall.put(nomePaciente, fila);
    }

    public void registrarAgendamentoFuturo(String nomePaciente) {
        Agendamento agendamento = new Agendamento(nomePaciente, true);
        agendamentos.put(nomePaciente, agendamento);
    }

    public void verificarSobreposicaoParaRecall(String nomePaciente) {
        FilaDeRecall fila = filaDeRecall.get(nomePaciente);
        Agendamento agendamento = agendamentos.get(nomePaciente);
        if (fila != null && agendamento != null) {
            RecallCancelado evento = domainService.verificarSobreposicao(fila, agendamento);
            this.ultimoEventoCancelado = evento;
        }
    }

    public Agendamento agendarViaRecall(String nomePaciente) {
        FilaDeRecall fila = filaDeRecall.get(nomePaciente);
        Agendamento novoAgendamento = new Agendamento(nomePaciente, false);
        agendamentos.put(nomePaciente + "_recall", novoAgendamento);
        RecallConvertido evento = domainService.converterRecall(fila, novoAgendamento);
        this.ultimoEventoConvertido = evento;
        return novoAgendamento;
    }

    public FilaDeRecall getFilaDeRecall(String nomePaciente) {
        return filaDeRecall.get(nomePaciente);
    }

    public Agendamento getAgendamentoRecall(String nomePaciente) {
        return agendamentos.get(nomePaciente + "_recall");
    }

    public RecallCancelado getUltimoEventoCancelado() {
        return ultimoEventoCancelado;
    }
}