package com.g4.odontohub.comissao_repasse.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
public class RegraComissao {

    private UUID id;
    private String nomeEspecialista;
    private String nomeProcedimento;
    private TipoComissao tipo;
    private BigDecimal valor;

    private RegraComissao() {}

    public static RegraComissao criarPercentual(String nomeEspecialista, String nomeProcedimento, BigDecimal percentual) {
        RegraComissao r = new RegraComissao();
        r.id = UUID.randomUUID();
        r.nomeEspecialista = nomeEspecialista;
        r.nomeProcedimento = nomeProcedimento;
        r.tipo = TipoComissao.PERCENTUAL;
        r.valor = percentual;
        return r;
    }

    public static RegraComissao criarValorFixo(String nomeEspecialista, String nomeProcedimento, BigDecimal valorFixo) {
        RegraComissao r = new RegraComissao();
        r.id = UUID.randomUUID();
        r.nomeEspecialista = nomeEspecialista;
        r.nomeProcedimento = nomeProcedimento;
        r.tipo = TipoComissao.VALOR_FIXO;
        r.valor = valorFixo;
        return r;
    }

    public BigDecimal calcularComissao(BigDecimal valorProcedimento) {
        if (tipo == TipoComissao.PERCENTUAL) {
            return valorProcedimento.multiply(valor)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}