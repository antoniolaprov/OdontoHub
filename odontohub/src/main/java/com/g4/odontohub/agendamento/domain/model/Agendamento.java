package com.g4.odontohub.agendamento.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Agendamento {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final String RESPONSAVEL_RECEPCAO = "Recepcionista";

    private final AgendamentoId id;
    private final PacienteId pacienteId;
    private final DentistaId dentistaId;
    private LocalDateTime dataHora;
    private final TipoAtendimento tipo;
    private StatusAgendamento status;
    private String motivoCancelamento;
    private String responsavelAlteracao;
    private LocalDateTime dataUltimaAlteracao;
    private final List<EntradaHistorico> historico = new ArrayList<>();

    public Agendamento(AgendamentoId id, PacienteId pacienteId, DentistaId dentistaId,
                       LocalDateTime dataHora, TipoAtendimento tipo) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.dentistaId = dentistaId;
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.status = StatusAgendamento.AGENDADO;
    }

    public void confirmar(String responsavel) {
        this.status = StatusAgendamento.CONFIRMADO;
        this.responsavelAlteracao = RESPONSAVEL_RECEPCAO;
        this.dataUltimaAlteracao = LocalDateTime.now();
        adicionarHistorico("Confirmado", RESPONSAVEL_RECEPCAO);
    }

    public void cancelar(String motivo, String responsavel) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Informe o motivo do cancelamento.");
        }
        this.status = StatusAgendamento.CANCELADO;
        this.motivoCancelamento = motivo;
        this.responsavelAlteracao = responsavel;
        this.dataUltimaAlteracao = LocalDateTime.now();
        adicionarHistorico("Cancelado: " + motivo, responsavel);
    }

    public void remarcar(LocalDateTime novaDataHora, String responsavel) {
        if (novaDataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível remarcar para datas passadas.");
        }
        this.dataHora = novaDataHora;
        this.status = StatusAgendamento.REMARCADO;
        this.responsavelAlteracao = RESPONSAVEL_RECEPCAO;
        this.dataUltimaAlteracao = LocalDateTime.now();
        adicionarHistorico("Remarcado para " + novaDataHora.format(FORMATTER), RESPONSAVEL_RECEPCAO);
    }

    private void adicionarHistorico(String acao, String responsavel) {
        historico.add(new EntradaHistorico(acao, responsavel, LocalDateTime.now()));
    }

    public AgendamentoId getId() { return id; }
    public PacienteId getPacienteId() { return pacienteId; }
    public DentistaId getDentistaId() { return dentistaId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public TipoAtendimento getTipo() { return tipo; }
    public StatusAgendamento getStatus() { return status; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public String getResponsavelAlteracao() { return responsavelAlteracao; }
    public LocalDateTime getDataUltimaAlteracao() { return dataUltimaAlteracao; }
    public List<EntradaHistorico> getHistorico() { return Collections.unmodifiableList(historico); }
}
