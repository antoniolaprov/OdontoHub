package com.g4.odontohub.relacionamentopaciente.churn.application;

import com.g4.odontohub.relacionamentopaciente.churn.domain.model.StatusChurn;
import com.g4.odontohub.relacionamentopaciente.churn.domain.repository.ChurnRepository;
import com.g4.odontohub.relacionamentopaciente.churn.domain.service.ChurnService;
import com.g4.odontohub.relacionamentopaciente.churn.infrastructure.persistence.InMemoryChurnRepository;

public class ChurnApplicationService {

    private final ChurnService service;

    public ChurnApplicationService() {
        this(new InMemoryChurnRepository());
    }

    public ChurnApplicationService(ChurnRepository repositorio) {
        this.service = new ChurnService(repositorio);
    }

    public void definirValorMedioHora(double valor) {
        service.definirValorMedioHora(valor);
    }

    public void definirSemAgendamentoFuturo(String paciente) {
        service.definirSemAgendamentoFuturo(paciente);
    }

    public void definirMesesSemRetorno(String paciente, int meses) {
        service.definirMesesSemRetorno(paciente, meses);
    }

    public void definirPlanoAtivo(String paciente, boolean ativo) {
        service.definirPlanoAtivo(paciente, ativo);
    }

    public void recalcular() {
        service.recalcularStatusChurn();
    }

    public void classificarManualmente(String paciente, String status) {
        service.classificarManualmente(paciente, StatusChurn.fromLabel(status));
    }

    public void reativar(String paciente) {
        service.reativar(paciente);
    }

    public StatusChurn statusDe(String paciente) {
        return service.statusDe(paciente);
    }

    public boolean temAlertaInatividade(String paciente) {
        return service.temAlertaInatividade(paciente);
    }

    public double calcularReceitaPerdida(int horas) {
        return service.calcularReceitaPerdida(horas);
    }

    public void registrarNoShow(String paciente) {
        service.registrarNoShow(paciente);
    }

    public void cancelarAgendamento(String paciente, String categoriaMotivo) {
        service.cancelarAgendamento(paciente, categoriaMotivo);
    }

    public String getUltimoRegistroAgendamento() {
        return service.getUltimoRegistroAgendamento();
    }

    public int getNoShowCount() {
        return service.getNoShowCount();
    }

    public int getCancelamentoCount() {
        return service.getCancelamentoCount();
    }
}
