package com.g4.odontohub.tratamento.application;

import com.g4.odontohub.tratamento.domain.PlanoTratamento;
import com.g4.odontohub.tratamento.domain.PlanoTratamentoRepository;
import com.g4.odontohub.tratamento.domain.Procedimento;

import java.util.List;

public class PlanoTratamentoService {

    private final PlanoTratamentoRepository repository;
    private final ProntuarioACL prontuarioACL;

    public PlanoTratamentoService(PlanoTratamentoRepository repository, ProntuarioACL prontuarioACL) {
        this.repository = repository;
        this.prontuarioACL = prontuarioACL;
    }

    public PlanoTratamento criarPlano(Long idPlano, Long pacienteId, List<Procedimento> procedimentos) {
        boolean possuiAnamnese = prontuarioACL.possuiAnamnese(pacienteId);
        PlanoTratamento plano = PlanoTratamento.criar(idPlano, pacienteId, procedimentos, possuiAnamnese);
        repository.salvar(plano);
        return plano;
    }

    public void cancelarProcedimento(Long planoId, String descricao, String justificativa) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        Procedimento procedimento = plano.getProcedimentoPorDescricao(descricao);
        procedimento.cancelar(justificativa);
        repository.salvar(plano);
    }

    public void concluirPlano(Long planoId) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        plano.concluir();
        repository.salvar(plano);
    }

    public void adicionarProcedimentoRetorno(Long planoId, Procedimento novoProcedimento) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        plano.adicionarProcedimentoRetorno(novoProcedimento);
        repository.salvar(plano);
    }

    public void excluirPlano(Long planoId) {
        PlanoTratamento plano = repository.buscarPorId(planoId);
        plano.validarExclusao();
        repository.deletar(planoId);
    }
}