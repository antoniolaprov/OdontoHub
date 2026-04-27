package com.g4.odontohub.fluxocaixa.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate root do contexto de Fluxo de Caixa.
 * AUDITORIA: Java puro — sem @Entity, @Table, @Id ou qualquer anotação JPA/Spring.
 * Apenas @Getter. Comportamentos expressos como métodos de negócio.
 */
@Getter
public class LancamentoCaixa {

    private String id;
    private TipoLancamento tipo;
    private BigDecimal valor;
    private String categoria;
    private String justificativa;
    private boolean geradoAutomaticamente;
    /** ID da parcela de origem, se o lançamento foi gerado por liquidação */
    private String parcelaOrigemId;
    private LocalDate data;

    private LancamentoCaixa() {}

    // ── Fábrica: entrada gerada automaticamente por liquidação de parcela ─────

    public static LancamentoCaixa criarEntradaAutomatica(BigDecimal valor, String parcelaOrigemId) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O valor do lançamento deve ser maior que zero");
        }
        if (parcelaOrigemId == null || parcelaOrigemId.isBlank()) {
            throw new DomainException("A parcela de origem é obrigatória para entradas automáticas");
        }

        LancamentoCaixa l = new LancamentoCaixa();
        l.id = UUID.randomUUID().toString();
        l.tipo = TipoLancamento.ENTRADA;
        l.valor = valor;
        l.categoria = "Liquidação de Parcela";
        l.justificativa = "Gerado automaticamente por liquidação da parcela " + parcelaOrigemId;
        l.geradoAutomaticamente = true;
        l.parcelaOrigemId = parcelaOrigemId;
        l.data = LocalDate.now();
        return l;
    }

    // ── Fábrica: saída avulsa registrada manualmente ──────────────────────────

    public static LancamentoCaixa criarSaidaAvulsa(BigDecimal valor, String categoria, String justificativa) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O valor do lançamento deve ser maior que zero");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new DomainException("A justificativa é obrigatória para saídas avulsas");
        }

        LancamentoCaixa l = new LancamentoCaixa();
        l.id = UUID.randomUUID().toString();
        l.tipo = TipoLancamento.SAIDA;
        l.valor = valor;
        l.categoria = categoria;
        l.justificativa = justificativa;
        l.geradoAutomaticamente = false;
        l.parcelaOrigemId = null;
        l.data = LocalDate.now();
        return l;
    }

    // ── Fábrica: saída gerada automaticamente (ex: reposição de estoque) ──────

    public static LancamentoCaixa criarSaidaAutomatica(BigDecimal valor, String categoria, String justificativa) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O valor do lançamento deve ser maior que zero");
        }

        LancamentoCaixa l = new LancamentoCaixa();
        l.id = UUID.randomUUID().toString();
        l.tipo = TipoLancamento.SAIDA;
        l.valor = valor;
        l.categoria = categoria;
        l.justificativa = justificativa;
        l.geradoAutomaticamente = true;
        l.parcelaOrigemId = null;
        l.data = LocalDate.now();
        return l;
    }

    // ── Comportamentos ───────────────────────────────────────────────────────

    /**
     * Impede edição de entradas geradas automaticamente.
     * Lança DomainException para qualquer tentativa de alterar o valor.
     */
    public void editarValor(BigDecimal novoValor) {
        if (this.geradoAutomaticamente && this.tipo == TipoLancamento.ENTRADA) {
            throw new DomainException("Entradas geradas automaticamente não podem ser editadas");
        }
        if (novoValor == null || novoValor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O valor do lançamento deve ser maior que zero");
        }
        this.valor = novoValor;
    }
}
