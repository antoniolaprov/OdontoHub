package com.g4.odontohub.fichaClinica.application;

import com.g4.odontohub.agendamento.domain.Agendamento;
import com.g4.odontohub.agendamento.domain.AgendamentoRepository;
import com.g4.odontohub.agendamento.domain.StatusAgendamento;
import com.g4.odontohub.prontuario.domain.*;
import com.g4.odontohub.shared.exception.DomainException;


public class FichaClinicaService {

    private final ProntuarioRepository prontuarioRepository;
    private final AgendamentoRepository agendamentoRepository;

    public FichaClinicaService(ProntuarioRepository prontuarioRepository,
                               AgendamentoRepository agendamentoRepository) {
        this.prontuarioRepository = prontuarioRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // 🔹 CENÁRIOS 1, 2 e 4
    public void registrarAtendimento(Long pacienteId,
                                 Long agendamentoId,
                                 Long fichaId,
                                 String evolucao) {

    Agendamento agendamento = agendamentoRepository.buscarPorId(agendamentoId)
            .orElseThrow(() -> new DomainException("Agendamento não encontrado"));

    if (!agendamento.getPacienteId().equals(pacienteId)) {
        throw new DomainException("Agendamento não pertence ao paciente informado");
    }

    if (agendamento.getStatus() != StatusAgendamento.CONFIRMADO) {
        throw new DomainException(
            "É necessário um agendamento confirmado para registrar atendimento"
        );
    }

    Prontuario prontuario = prontuarioRepository
            .buscarPorPacienteId(pacienteId)
            .orElseGet(() -> Prontuario.criar(null, pacienteId));

    // 🔥 AQUI É A MUDANÇA IMPORTANTE
    prontuario.adicionarFicha(fichaId, evolucao);

    prontuarioRepository.salvar(prontuario);
}
    // 🔹 CENÁRIO 3
    public void editarEvolucao(Long prontuarioId, Long fichaId, String novaEvolucao) {

        Prontuario prontuario = prontuarioRepository.buscarPorId(prontuarioId)
                .orElseThrow(() -> new DomainException("Prontuário não encontrado"));

        prontuario.editarEvolucaoFicha(fichaId, novaEvolucao);

        prontuarioRepository.salvar(prontuario);
    }

    // 🔹 EXTRA (provavelmente usado nos steps)
    public void confirmarFicha(Long prontuarioId, Long fichaId) {
        Prontuario prontuario = prontuarioRepository.buscarPorId(prontuarioId)
                .orElseThrow(() -> new DomainException("Prontuário não encontrado"));

        prontuario.confirmarFicha(fichaId);

        prontuarioRepository.salvar(prontuario);
    }

    // 🔹 CENÁRIO 5
    public void encerrarProntuario(Long pacienteId, String justificativa) {
        Prontuario prontuario = prontuarioRepository.buscarPorPacienteId(pacienteId)
                .orElseThrow(() -> new DomainException("Prontuário não encontrado"));

        prontuario.encerrar(justificativa);

        prontuarioRepository.salvar(prontuario);
    }

    // 🔹 CENÁRIO 6
    public void excluirProntuario(Long pacienteId) {
        throw new DomainException(
            "Prontuários não podem ser excluídos, apenas encerrados"
        );
    }
}