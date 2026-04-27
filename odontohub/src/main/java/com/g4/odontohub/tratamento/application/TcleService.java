package com.g4.odontohub.tratamento.application;

import com.g4.odontohub.shared.exception.DomainException;
import com.g4.odontohub.tratamento.domain.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service de aplicação que orquestra o ciclo de vida do TCLE.
 * Sem @Service ou @Component (DDD estrito).
 */
public class TcleService {

    private final TcleRepository tcleRepository;
    private final PlanoTratamentoRepository planoRepository;
    private final AtomicLong idSequence = new AtomicLong(100);

    public TcleService(TcleRepository tcleRepository, PlanoTratamentoRepository planoRepository) {
        this.tcleRepository = tcleRepository;
        this.planoRepository = planoRepository;
    }

    /**
     * Registra e assina um TCLE para o plano indicado.
     */
    public Tcle registrarAssinado(Long planoId, String nomePaciente, LocalDate dataAssinatura) {
        Tcle tcle = Tcle.criar(idSequence.getAndIncrement(), planoId, nomePaciente);
        tcle.assinar(dataAssinatura);
        tcleRepository.salvar(tcle);
        return tcle;
    }

    /**
     * Tenta registrar TCLE sem vínculo de plano (deve lançar DomainException).
     */
    public Tcle registrarSemPlano(String nomePaciente) {
        // Tcle.criar com planoId null lança DomainException imediatamente
        return Tcle.criar(idSequence.getAndIncrement(), null, nomePaciente);
    }

    /**
     * Marca um TCLE como excluído — proibido se assinado.
     */
    public void excluirTcle(Long tcleId) {
        Tcle tcle = tcleRepository.buscarPorId(tcleId)
                .orElseThrow(() -> new DomainException("TCLE não encontrado: " + tcleId));
        tcle.validarExclusao();
        tcleRepository.deletar(tcleId);
    }

    /**
     * Substitui o TCLE assinado atual por um novo TCLE com status PENDENTE.
     *
     * @return o novo TCLE criado com status PENDENTE
     */
    public Tcle substituir(Long tcleAntigoId, String justificativa) {
        Tcle tcleAntigo = tcleRepository.buscarPorId(tcleAntigoId)
                .orElseThrow(() -> new DomainException("TCLE não encontrado: " + tcleAntigoId));
        tcleAntigo.substituir(justificativa);
        tcleRepository.salvar(tcleAntigo);

        Tcle novoTcle = Tcle.criar(idSequence.getAndIncrement(), tcleAntigo.getPlanoId(), tcleAntigo.getNomePaciente());
        tcleRepository.salvar(novoTcle);
        return novoTcle;
    }

    /**
     * Realiza um procedimento no plano, exigindo TCLE assinado previamente.
     */
    public void realizarProcedimento(Long planoId, String descricaoProcedimento) {
        Optional<Tcle> tcleAssinado = tcleRepository.buscarAssinadoPorPlano(planoId);
        if (tcleAssinado.isEmpty() || !tcleAssinado.get().estaAssinado()) {
            throw new DomainException("É necessário um TCLE assinado para registrar procedimentos realizados");
        }
        PlanoTratamento plano = planoRepository.buscarPorId(planoId);
        Procedimento procedimento = plano.getProcedimentoPorDescricao(descricaoProcedimento);
        procedimento.realizar();
        planoRepository.salvar(plano);
    }

    /**
     * Tenta realizar um procedimento sem TCLE assinado (cenário de bloqueio).
     */
    public void tentarRealizarProcedimentoSemTcle(Long planoId, String descricaoProcedimento) {
        realizarProcedimento(planoId, descricaoProcedimento);
    }
}
