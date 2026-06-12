package com.g4.odontohub.pagamento.infrastructure.persistence.jpa;

import com.g4.odontohub.pagamento.domain.model.FormaPagamento;
import com.g4.odontohub.pagamento.domain.model.Pagamento;
import com.g4.odontohub.pagamento.domain.model.PagamentoId;
import com.g4.odontohub.pagamento.domain.model.StatusPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "pagamento")
public class PagamentoJpaEntity {

    @Id
    private Long id;
    private String parcelaReferencia;
    private double valorRecebido;
    private LocalDate dataPagamento;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FormaPagamento formaPagamento;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusPagamento status;
    private String observacao;
    private String justificativaCancelamento;

    protected PagamentoJpaEntity() {
    }

    public static PagamentoJpaEntity fromDomain(Pagamento pagamento) {
        PagamentoJpaEntity e = new PagamentoJpaEntity();
        e.id = pagamento.getId().id();
        e.parcelaReferencia = pagamento.getParcelaReferencia();
        e.valorRecebido = pagamento.getValorRecebido();
        e.dataPagamento = pagamento.getDataPagamento();
        e.formaPagamento = pagamento.getFormaPagamento();
        e.status = pagamento.getStatus();
        e.observacao = pagamento.getObservacao();
        e.justificativaCancelamento = pagamento.getJustificativaCancelamento();
        return e;
    }

    public Pagamento toDomain() {
        return Pagamento.reconstituir(new PagamentoId(id), parcelaReferencia, valorRecebido,
                dataPagamento, formaPagamento, status, observacao, justificativaCancelamento);
    }
}
