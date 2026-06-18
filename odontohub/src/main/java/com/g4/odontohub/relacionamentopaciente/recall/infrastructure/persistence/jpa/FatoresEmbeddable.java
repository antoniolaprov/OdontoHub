package com.g4.odontohub.relacionamentopaciente.recall.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.recall.domain.model.FatoresPriorizacao;
import jakarta.persistence.Embeddable;

/** Mapeamento de persistência dos fatores de priorização do recall (Data Mapper). */
@Embeddable
public class FatoresEmbeddable {

    private int mesesSemRetorno;
    private int tratamentosPendentes;
    private boolean procedimentoCritico;
    private boolean inadimplente;
    private int historicoFaltas;
    private int cancelamentosRecorrentes;
    private boolean riscoClinicoAlto;
    private double potencialFinanceiro;
    private boolean tratamentoInterrompido;
    private boolean pacienteEvadido;

    protected FatoresEmbeddable() {
    }

    static FatoresEmbeddable fromDomain(FatoresPriorizacao f) {
        FatoresEmbeddable e = new FatoresEmbeddable();
        FatoresPriorizacao fatores = f == null ? FatoresPriorizacao.vazio() : f;
        e.mesesSemRetorno = fatores.mesesSemRetorno();
        e.tratamentosPendentes = fatores.tratamentosPendentes();
        e.procedimentoCritico = fatores.procedimentoCritico();
        e.inadimplente = fatores.inadimplente();
        e.historicoFaltas = fatores.historicoFaltas();
        e.cancelamentosRecorrentes = fatores.cancelamentosRecorrentes();
        e.riscoClinicoAlto = fatores.riscoClinicoAlto();
        e.potencialFinanceiro = fatores.potencialFinanceiro();
        e.tratamentoInterrompido = fatores.tratamentoInterrompido();
        e.pacienteEvadido = fatores.pacienteEvadido();
        return e;
    }

    FatoresPriorizacao toDomain() {
        return new FatoresPriorizacao(mesesSemRetorno, tratamentosPendentes, procedimentoCritico, inadimplente,
                historicoFaltas, cancelamentosRecorrentes, riscoClinicoAlto, potencialFinanceiro,
                tratamentoInterrompido, pacienteEvadido);
    }
}
