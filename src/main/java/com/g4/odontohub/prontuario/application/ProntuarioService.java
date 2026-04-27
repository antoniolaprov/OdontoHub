package com.g4.odontohub.prontuario.application;

import com.g4.odontohub.prontuario.domain.Prontuario;
import com.g4.odontohub.prontuario.domain.ProntuarioRepository;
import com.g4.odontohub.shared.exception.DomainException;

import java.util.concurrent.atomic.AtomicLong;

public class ProntuarioService {

    private final ProntuarioRepository repository;
    private final AtomicLong prontuarioSequence = new AtomicLong(1);
    private final AtomicLong fichaSequence = new AtomicLong(1);

    public ProntuarioService(ProntuarioRepository repository) {
        this.repository = repository;
    }

    public Prontuario registrarFichaClinica(Long pacienteId, boolean agendamentoConfirmado, String evolucao) {
        if (!agendamentoConfirmado) {
            throw new DomainException("É necessário um agendamento confirmado para registrar atendimento");
        }
        Prontuario prontuario = repository.buscarPorPacienteId(pacienteId)
                .orElseGet(() -> Prontuario.criar(prontuarioSequence.getAndIncrement(), pacienteId));
        prontuario.adicionarFicha(fichaSequence.getAndIncrement(), evolucao);
        return repository.salvar(prontuario);
    }

    public Prontuario confirmarFicha(Long prontuarioId, Long fichaId) {
        Prontuario prontuario = buscarOuFalhar(prontuarioId);
        prontuario.confirmarFicha(fichaId);
        return repository.salvar(prontuario);
    }

    public Prontuario editarEvolucao(Long prontuarioId, Long fichaId, String novaEvolucao) {
        Prontuario prontuario = buscarOuFalhar(prontuarioId);
        prontuario.editarEvolucaoFicha(fichaId, novaEvolucao);
        return repository.salvar(prontuario);
    }

    public Prontuario encerrar(Long prontuarioId, String justificativa) {
        Prontuario prontuario = buscarOuFalhar(prontuarioId);
        prontuario.encerrar(justificativa);
        return repository.salvar(prontuario);
    }

    public void excluir(Long prontuarioId) {
        throw new DomainException("Prontuários não podem ser excluídos, apenas encerrados");
    }

    private Prontuario buscarOuFalhar(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Prontuário não encontrado: " + id));
    }
}
