package com.g4.odontohub.financeiro.domain.model;

/**
 * Parcela em cobrança com encargos calculados automaticamente pelo sistema.
 * Os valores de juros e multa não podem ser editados manualmente pela recepção.
 */
public class ParcelaCobranca {

    private final double valor;
    private final int diasAtraso;
    private final double multa;
    private final double juros;
    private StatusParcela status;

    public ParcelaCobranca(double valor, int diasAtraso, double multa, double juros) {
        this.valor = valor;
        this.diasAtraso = diasAtraso;
        this.multa = multa;
        this.juros = juros;
        this.status = StatusParcela.VENCIDA;
    }

    /** Cálculo travado: a recepção não pode alterar juros/multa. */
    public void editarJurosOuMulta(double novoValor) {
        throw new IllegalStateException(
                "Juros e multa são calculados automaticamente e não podem ser editados manualmente");
    }

    /** Reconstitui a parcela a partir da persistência (camada de infraestrutura). */
    public static ParcelaCobranca reconstituir(double valor, int diasAtraso, double multa,
                                               double juros, StatusParcela status) {
        ParcelaCobranca p = new ParcelaCobranca(valor, diasAtraso, multa, juros);
        p.status = status;
        return p;
    }

    public void substituir() {
        this.status = StatusParcela.SUBSTITUIDA;
    }

    public void marcarPendente() {
        this.status = StatusParcela.PENDENTE;
    }

    public boolean contaNoFluxoCaixa() {
        return status != StatusParcela.SUBSTITUIDA;
    }

    public double getValor() { return valor; }
    public int getDiasAtraso() { return diasAtraso; }
    public double getMulta() { return multa; }
    public double getJuros() { return juros; }
    public StatusParcela getStatus() { return status; }
}
