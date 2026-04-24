package com.g4.odontohub.agendamento.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root do contexto de Agendamento.
 * AUDITORIA: Java puro — sem @Entity, @Table, @Id ou qualquer anotação JPA/Spring.
 * Apenas @Getter. Comportamentos expressos como métodos de negócio.
 */
@Getter
public class Agendamento {

    private Long id;
    private Long pacienteId;
    private LocalDateTime dataHora;
    private TipoAgendamento tipo;
    private StatusAgendamento status;
    private boolean autorizadoPorDentista;
    private String responsavel;
    private List<HistoricoStatus> historicoStatus;

    private Agendamento() {}

    // ── Fábrica ──────────────────────────────────────────────────────────────
    // Regras táticas aplicadas aqui: data passada e inadimplência (sem autorização)

    public static Agendamento criar(Long id,
                                    Long pacienteId,
                                    LocalDateTime dataHora,
                                    boolean possuiPlanoAtivo,
                                    boolean pacienteInadimplente,
                                    boolean autorizadoPorDentista,
                                    String responsavel) {

        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new DomainException("Não é permitido registrar agendamentos em datas passadas");
        }

        if (pacienteInadimplente && !autorizadoPorDentista) {
            throw new DomainException("Paciente com inadimplência ativa");
        }

        Agendamento a = new Agendamento();
        a.id = id;
        a.pacienteId = pacienteId;
        a.dataHora = dataHora;
        a.tipo = possuiPlanoAtivo ? TipoAgendamento.RETORNO : TipoAgendamento.CONSULTA;
        a.status = StatusAgendamento.PENDENTE;
        a.autorizadoPorDentista = autorizadoPorDentista;
        a.responsavel = responsavel;
        a.historicoStatus = new ArrayList<>();
        a.historicoStatus.add(new HistoricoStatus(StatusAgendamento.PENDENTE, responsavel, null));
        return a;
    }

    // ── Comportamentos ───────────────────────────────────────────────────────

    public void confirmar(String responsavel) {
        if (this.status != StatusAgendamento.PENDENTE) {
            throw new DomainException("Apenas agendamentos pendentes podem ser confirmados");
        }
        this.status = StatusAgendamento.CONFIRMADO;
        this.historicoStatus.add(new HistoricoStatus(StatusAgendamento.CONFIRMADO, responsavel, null));
    }

    public void cancelar(String motivo, String responsavel) {
        this.status = StatusAgendamento.CANCELADO;
        this.historicoStatus.add(new HistoricoStatus(StatusAgendamento.CANCELADO, responsavel, motivo));
    }

    public List<HistoricoStatus> getHistoricoStatus() {
        return Collections.unmodifiableList(historicoStatus);
    }
}
