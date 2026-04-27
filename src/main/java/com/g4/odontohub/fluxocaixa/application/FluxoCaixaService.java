package com.g4.odontohub.fluxocaixa.application;

import com.g4.odontohub.fluxocaixa.domain.LancamentoCaixa;
import com.g4.odontohub.fluxocaixa.domain.LancamentoCaixaRepository;
import com.g4.odontohub.fluxocaixa.domain.TipoLancamento;
import com.g4.odontohub.shared.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Serviço de aplicação do contexto de Fluxo de Caixa.
 * Orquestra as regras de negócio delegando ao domínio e ao repositório.
 * AUDITORIA: Sem @Service, @Component ou qualquer anotação Spring aqui.
 */
public class FluxoCaixaService {

    private final LancamentoCaixaRepository repository;
    /** Repositório adicional de parcelas previstas (projeção de saldo) */
    private final ParcelaVencimentoRepository parcelaRepo;

    public FluxoCaixaService(LancamentoCaixaRepository repository,
                              ParcelaVencimentoRepository parcelaRepo) {
        this.repository = repository;
        this.parcelaRepo = parcelaRepo;
    }

    // ── Casos de uso ─────────────────────────────────────────────────────────

    /** Liquida uma parcela: cria entrada automática no fluxo de caixa. */
    public LancamentoCaixa liquidarParcela(String parcelaId, BigDecimal valor) {
        LancamentoCaixa lancamento = LancamentoCaixa.criarEntradaAutomatica(valor, parcelaId);
        return repository.salvar(lancamento);
    }

    /** Registra uma saída avulsa (manual). Justificativa obrigatória validada no domínio. */
    public LancamentoCaixa registrarSaidaAvulsa(BigDecimal valor, String categoria, String justificativa) {
        LancamentoCaixa lancamento = LancamentoCaixa.criarSaidaAvulsa(valor, categoria, justificativa);
        return repository.salvar(lancamento);
    }

    /** Registra uma saída gerada automaticamente (ex: reposição de estoque). */
    public LancamentoCaixa registrarSaidaAutomatica(BigDecimal valor, String categoria, String justificativa) {
        LancamentoCaixa lancamento = LancamentoCaixa.criarSaidaAutomatica(valor, categoria, justificativa);
        return repository.salvar(lancamento);
    }

    /** Tenta editar o valor de um lançamento. Lança DomainException se automático. */
    public LancamentoCaixa editarValor(String lancamentoId, BigDecimal novoValor) {
        LancamentoCaixa lancamento = repository.buscarPorId(lancamentoId)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado: " + lancamentoId));
        lancamento.editarValor(novoValor);
        return repository.salvar(lancamento);
    }

    /**
     * Calcula a projeção de saldo até a data limite.
     * Entradas = parcelas a vencer no período.
     * Saídas = saídas já registradas no repositório até a data.
     */
    public ProjecaoSaldo projetarSaldo(LocalDate dataLimite) {
        BigDecimal entradas = parcelaRepo.somarParcelasAVencer(dataLimite);

        List<LancamentoCaixa> lancamentos = repository.listarTodos();
        BigDecimal saidas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoLancamento.SAIDA)
                .filter(l -> !l.getData().isAfter(dataLimite))
                .map(LancamentoCaixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = entradas.subtract(saidas);
        return new ProjecaoSaldo(entradas, saidas, saldo);
    }

    public LancamentoCaixa buscar(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado: " + id));
    }

    public List<LancamentoCaixa> listarTodos() {
        return repository.listarTodos();
    }
}
