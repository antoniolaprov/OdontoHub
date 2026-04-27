package com.g4.odontohub.comissao_repasse.application;

import com.g4.odontohub.comissao_repasse.domain.*;
import com.g4.odontohub.shared.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ComissaoService {

    private final RegraComissaoRepository regraComissaoRepository;
    private final ComissaoRepository comissaoRepository;
    private final RepasseRepository repasseRepository;
    private final LancamentoCaixaRepository lancamentoCaixaRepository;

    public ComissaoService(RegraComissaoRepository regraComissaoRepository,
                            ComissaoRepository comissaoRepository,
                            RepasseRepository repasseRepository,
                            LancamentoCaixaRepository lancamentoCaixaRepository) {
        this.regraComissaoRepository = regraComissaoRepository;
        this.comissaoRepository = comissaoRepository;
        this.repasseRepository = repasseRepository;
        this.lancamentoCaixaRepository = lancamentoCaixaRepository;
    }

    public void cadastrarRegraPercentual(String nomeEspecialista, String nomeProcedimento, BigDecimal percentual) {
        RegraComissao regra = RegraComissao.criarPercentual(nomeEspecialista, nomeProcedimento, percentual);
        regraComissaoRepository.salvar(regra);
    }

    public void cadastrarRegraValorFixo(String nomeEspecialista, String nomeProcedimento, BigDecimal valorFixo) {
        RegraComissao regra = RegraComissao.criarValorFixo(nomeEspecialista, nomeProcedimento, valorFixo);
        regraComissaoRepository.salvar(regra);
    }

    public Comissao registrarProcedimentoRealizadoPorEspecialista(String nomeProcedimento,
                                                                    String nomeEspecialista,
                                                                    BigDecimal valorProcedimento) {
        RegraComissao regra = regraComissaoRepository
                .buscarPorEspecialistaEProcedimento(nomeEspecialista, nomeProcedimento)
                .orElseThrow(() -> new DomainException(
                        "Regra de comissão não encontrada para " + nomeEspecialista + " / " + nomeProcedimento));
        BigDecimal valorComissao = regra.calcularComissao(valorProcedimento);
        Comissao comissao = Comissao.criar(nomeEspecialista, nomeProcedimento, valorComissao);
        comissaoRepository.salvar(comissao);
        return comissao;
    }

    public void registrarPagamentoPaciente(String nomeEspecialista, String nomeProcedimento) {
        Comissao comissao = comissaoRepository
                .buscarPorEspecialistaEProcedimento(nomeEspecialista, nomeProcedimento)
                .orElseThrow(() -> new DomainException("Comissão não encontrada"));
        comissao.liberar();
        comissaoRepository.salvar(comissao);
    }

    public void tentarRegistrarRepasse(String nomeEspecialista) {
        Comissao comissao = comissaoRepository
                .buscarPorEspecialista(nomeEspecialista)
                .orElseThrow(() -> new DomainException("Comissão não encontrada para " + nomeEspecialista));
        comissao.verificarPodeRepassar();
    }

    public Repasse registrarRepasse(String nomeEspecialista, BigDecimal valor, LocalDate data) {
        Comissao comissao = comissaoRepository
                .buscarPorEspecialista(nomeEspecialista)
                .orElseThrow(() -> new DomainException("Comissão não encontrada para " + nomeEspecialista));
        comissao.marcarComoRepassada();
        comissaoRepository.salvar(comissao);

        Repasse repasse = Repasse.criar(nomeEspecialista, valor, data);
        repasseRepository.salvar(repasse);

        LancamentoCaixa lancamento = LancamentoCaixa.criarSaida(
                "Repasse especialista: " + nomeEspecialista, valor, data);
        lancamentoCaixaRepository.salvar(lancamento);

        return repasse;
    }

    public void excluirRepasse(String nomeEspecialista) {
        Repasse repasse = repasseRepository.buscarPorEspecialista(nomeEspecialista)
                .orElseThrow(() -> new DomainException("Repasse não encontrado"));
        repasse.excluir();
    }

    public void estornarRepasse(String nomeEspecialista, String justificativa) {
        Repasse repasse = repasseRepository.buscarPorEspecialista(nomeEspecialista)
                .orElseThrow(() -> new DomainException("Repasse não encontrado"));
        repasse.estornar(justificativa);
        repasseRepository.salvar(repasse);
    }
}