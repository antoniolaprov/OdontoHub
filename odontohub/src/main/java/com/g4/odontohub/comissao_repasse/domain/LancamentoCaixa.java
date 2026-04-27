package com.g4.odontohub.comissao_repasse.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class LancamentoCaixa {

    private UUID id;
    private String descricao;
    private BigDecimal valor;
    private LocalDate data;
    private TipoLancamento tipo;

    private LancamentoCaixa() {}

    public static LancamentoCaixa criarSaida(String descricao, BigDecimal valor, LocalDate data) {
        LancamentoCaixa l = new LancamentoCaixa();
        l.id = UUID.randomUUID();
        l.descricao = descricao;
        l.valor = valor;
        l.data = data;
        l.tipo = TipoLancamento.SAIDA;
        return l;
    }
}