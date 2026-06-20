package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import com.g4.odontohub.financeiro.domain.model.LancamentoFinanceiro;
import com.g4.odontohub.financeiro.domain.model.LancamentoId;
import com.g4.odontohub.financeiro.domain.model.TipoLancamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "lancamento_financeiro")
public class LancamentoFinanceiroJpaEntity {

    @Id
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoLancamento tipo;
    private double valor;
    private LocalDate data;
    private String categoria;
    private String descricao;
    private boolean geradoAutomaticamente;
    /**
     * Coluna herdada de uma versão anterior do schema (H2 durável, NOT NULL).
     * O domínio distingue previsto/confirmado pela própria data (data &gt; hoje),
     * não por este campo — mas a coluna precisa ser preenchida para o INSERT não
     * violar o NOT NULL. Derivamos o valor da data, mantendo-o coerente.
     */
    private boolean previsto;

    protected LancamentoFinanceiroJpaEntity() {
    }

    public static LancamentoFinanceiroJpaEntity fromDomain(LancamentoFinanceiro l) {
        LancamentoFinanceiroJpaEntity e = new LancamentoFinanceiroJpaEntity();
        e.id = l.getId().id();
        e.tipo = l.getTipo();
        e.valor = l.getValor();
        e.data = l.getData();
        e.categoria = l.getCategoria();
        e.descricao = l.getDescricao();
        e.geradoAutomaticamente = l.isGeradoAutomaticamente();
        e.previsto = l.getData() != null && l.getData().isAfter(LocalDate.now());
        return e;
    }

    public LancamentoFinanceiro toDomain() {
        return new LancamentoFinanceiro(new LancamentoId(id), tipo, valor, data,
                categoria, descricao, geradoAutomaticamente);
    }
}
