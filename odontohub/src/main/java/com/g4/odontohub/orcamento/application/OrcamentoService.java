package com.g4.odontohub.orcamento.application;

import com.g4.odontohub.orcamento.domain.ItemOrcamento;
import com.g4.odontohub.orcamento.domain.Orcamento;
import com.g4.odontohub.orcamento.domain.OrcamentoRepository;
import com.g4.odontohub.orcamento.domain.StatusOrcamento;
import com.g4.odontohub.shared.exception.DomainException;
import com.g4.odontohub.tratamento.domain.PlanoTratamento;
import com.g4.odontohub.tratamento.domain.PlanoTratamentoRepository;
import com.g4.odontohub.tratamento.domain.Procedimento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final PlanoTratamentoRepository planoRepository;
    private final AtomicLong sequence = new AtomicLong(1);

    public OrcamentoService(OrcamentoRepository orcamentoRepository, PlanoTratamentoRepository planoRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.planoRepository = planoRepository;
    }

    public Orcamento gerarAutomatico(Long planoId) {
        PlanoTratamento plano = planoRepository.buscarPorId(planoId);
        if (plano == null) {
            throw new DomainException("Plano de tratamento não encontrado: " + planoId);
        }

        // Usar getProcedimentoPorDescricao para cada procedimento conhecido
        List<ItemOrcamento> itens = new ArrayList<>();
        for (String desc : List.of("Extração", "Limpeza")) {
            try {
                Procedimento p = plano.getProcedimentoPorDescricao(desc);
                itens.add(ItemOrcamento.criar(p.getDescricao(), 0f));
            } catch (DomainException e) {
                // Procedimento não encontrado, continuar
            }
        }

        Orcamento orcamento = Orcamento.criar(
                sequence.getAndIncrement(),
                planoId,
                plano.getPacienteId(),
                itens);

        orcamentoRepository.salvar(orcamento);
        return orcamento;
    }

    public Orcamento gerarComValores(Long planoId) {
        PlanoTratamento plano = planoRepository.buscarPorId(planoId);
        if (plano == null) {
            throw new DomainException("Plano de tratamento não encontrado: " + planoId);
        }

        // Usar getProcedimentoPorDescricao para cada procedimento conhecido
        List<ItemOrcamento> itens = new ArrayList<>();
        for (String desc : List.of("Extração", "Limpeza")) {
            try {
                Procedimento p = plano.getProcedimentoPorDescricao(desc);
                float valor = desc.equals("Extração") ? 250f : 150f;
                itens.add(ItemOrcamento.criar(p.getDescricao(), valor));
            } catch (DomainException e) {
                // Procedimento não encontrado, continuar
            }
        }

        Orcamento orcamento = Orcamento.criar(
                sequence.getAndIncrement(),
                planoId,
                plano.getPacienteId(),
                itens);

        orcamentoRepository.salvar(orcamento);
        return orcamento;
    }

    public void validarAprovacao(Long orcamentoId) {
        Orcamento orcamento = orcamentoRepository.buscarPorId(orcamentoId);
        if (orcamento == null) {
            throw new DomainException("Orçamento não encontrado: " + orcamentoId);
        }
        if (orcamento.getStatus() != StatusOrcamento.APROVADO) {
            throw new DomainException("O orçamento deve ser aprovado pelo paciente antes de iniciar procedimentos");
        }
    }

    public Orcamento aprovar(Long orcamentoId, LocalDate data, String forma) {
        Orcamento orcamento = orcamentoRepository.buscarPorId(orcamentoId);
        if (orcamento == null) {
            throw new DomainException("Orçamento não encontrado: " + orcamentoId);
        }
        orcamento.aprovar(data, forma);
        orcamentoRepository.salvar(orcamento);
        return orcamento;
    }

    public Orcamento alterarItem(Long orcamentoId, String descricaoItem, float novoValor) {
    Orcamento orcamento = orcamentoRepository.buscarPorId(orcamentoId);
    if (orcamento == null) {
        throw new DomainException("Orçamento não encontrado: " + orcamentoId);
    }

    orcamento.alterarItem(descricaoItem, novoValor);

    orcamentoRepository.salvar(orcamento);
    return orcamento;
}

    public Orcamento gerarComplementar(Long planoId, float valorAdicional) {
        PlanoTratamento plano = planoRepository.buscarPorId(planoId);
        if (plano == null) {
            throw new DomainException("Plano de tratamento não encontrado: " + planoId);
        }

        ItemOrcamento item = ItemOrcamento.criar("Restauração", valorAdicional);
        Orcamento orcamento = Orcamento.criarComplementar(
                sequence.getAndIncrement(),
                planoId,
                plano.getPacienteId(),
                List.of(item));

        orcamentoRepository.salvar(orcamento);
        return orcamento;
    }

    public Orcamento buscarPorId(Long id) {
        return orcamentoRepository.buscarPorId(id);
    }

    public Orcamento buscarPorPlano(Long planoId) {
        return orcamentoRepository.buscarPorPlanoTratamentoId(planoId).orElse(null);
    }
}
