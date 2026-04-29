package com.g4.odontohub.prontuarioclinico.domain.service;

import com.g4.odontohub.prontuarioclinico.application.PlanoTratamentoRepository;
import com.g4.odontohub.prontuarioclinico.domain.model.PlanoTratamento;
import com.g4.odontohub.prontuarioclinico.domain.model.Procedimento;
import com.g4.odontohub.prontuarioclinico.domain.model.VOsPlano.*;

import java.util.Date;

public class PlanoTratamentoService {
    private final AnamneseService anamneseService;
    private final PlanoTratamentoRepository repository;

    public PlanoTratamentoService(AnamneseService anamneseService, PlanoTratamentoRepository repository) {
        this.anamneseService = anamneseService;
        this.repository = repository;
    }

    public PlanoId criarPlano(Long pacienteId, Long dentistaId) {
        if (!anamneseService.anamneseExiste(pacienteId)) {
            throw new IllegalStateException("Paciente não possui anamnese registrada");
        }
        PlanoId id = new PlanoId(System.currentTimeMillis());
        PlanoTratamento plano = new PlanoTratamento(id, new Planotratamentopaciente(pacienteId), new Planotratamentodentista(dentistaId));
        repository.salvar(plano);
        return id;
    }

    public void adicionarProcedimento(PlanoId planoId, String nomeProcedimento, String tipoProcedimento) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        Procedimento proc = new Procedimento(new ProcedimentoId(System.currentTimeMillis() + nomeProcedimento.hashCode()), nomeProcedimento, tipoProcedimento);
        plano.adicionarProcedimento(proc);
        repository.salvar(plano);
    }

    public void realizarProcedimento(PlanoId planoId, ProcedimentoId procedimentoId, String descricaoEvolucao, Long agendamentoId, String executor) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        Procedimento proc = plano.buscarProcedimento(procedimentoId);
        
        proc.realizar(new EvolucaoClinica(descricaoEvolucao, executor, new Date()), new ProcedimentoAgendamentoVinculado(agendamentoId));
        repository.salvar(plano);
    }

    public void cancelarProcedimento(PlanoId planoId, ProcedimentoId procedimentoId, String justificativa) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        Procedimento proc = plano.buscarProcedimento(procedimentoId);
        proc.cancelar(justificativa);
        repository.salvar(plano);
    }

    public void excluirProcedimento(PlanoId planoId, ProcedimentoId procedimentoId, String justificativa, String responsavel) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        plano.excluirProcedimento(procedimentoId, justificativa, responsavel);
        repository.salvar(plano);
    }

    public void encerrarPlano(PlanoId planoId, String justificativa) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        plano.encerrarPlano(justificativa);
        repository.salvar(plano);
    }

    public void excluirPlano(PlanoId planoId) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        plano.validarPodeSerExcluido();
        repository.remover(planoId);
    }
}