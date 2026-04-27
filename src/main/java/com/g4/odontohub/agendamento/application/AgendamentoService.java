package com.g4.odontohub.agendamento.application;

import com.g4.odontohub.agendamento.domain.Agendamento;
import com.g4.odontohub.agendamento.domain.AgendamentoRepository;
import com.g4.odontohub.shared.exception.DomainException;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço de aplicação do contexto de Agendamento.
 * Orquestra as regras de negócio delegando ao domínio e ao repositório.
 * AUDITORIA: Sem @Service, @Component ou qualquer anotação Spring aqui.
 */
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final AtomicLong sequence = new AtomicLong(1);

    public AgendamentoService(AgendamentoRepository repository) {
        this.repository = repository;
    }

    public Agendamento registrar(Long pacienteId,
                                  LocalDateTime dataHora,
                                  boolean possuiPlanoAtivo,
                                  boolean pacienteInadimplente,
                                  boolean autorizadoPorDentista,
                                  String responsavel) {
        // Conflito de horário: depende de repositório, fica no service
        if (repository.existeConflito(dataHora)) {
            throw new DomainException("Já existe um agendamento para este horário");
        }

        // Demais regras (data passada, inadimplência) são invariantes do aggregate
        Agendamento agendamento = Agendamento.criar(
                sequence.getAndIncrement(),
                pacienteId,
                dataHora,
                possuiPlanoAtivo,
                pacienteInadimplente,
                autorizadoPorDentista,
                responsavel);

        return repository.salvar(agendamento);
    }

    public Agendamento confirmar(Long id, String responsavel) {
        Agendamento agendamento = buscarOuFalhar(id);
        agendamento.confirmar(responsavel);
        return repository.salvar(agendamento);
    }

    public Agendamento cancelar(Long id, String motivo, String responsavel) {
        Agendamento agendamento = buscarOuFalhar(id);
        agendamento.cancelar(motivo, responsavel);
        return repository.salvar(agendamento);
    }

    private Agendamento buscarOuFalhar(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Agendamento não encontrado: " + id));
    }
}
