package com.g4.odontohub.tratamento.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlanoTratamento {
    private Long id;
    private Long pacienteId;
    private StatusPlano status;
    private Integer versaoAtual;
    private List<Procedimento> procedimentos;
    private List<String> historicoVersoes; // Simplificado para manter o escopo do teste

    private PlanoTratamento() {}

    public static PlanoTratamento criar(Long id, Long pacienteId, List<Procedimento> procedimentos, boolean possuiAnamnese) {
        if (!possuiAnamnese) {
            throw new DomainException("É necessário registrar a anamnese antes de criar um plano de tratamento");
        }
        PlanoTratamento plano = new PlanoTratamento();
        plano.id = id;
        plano.pacienteId = pacienteId;
        plano.status = StatusPlano.ATIVO;
        plano.versaoAtual = 1;
        plano.procedimentos = new ArrayList<>(procedimentos);
        plano.historicoVersoes = new ArrayList<>();
        return plano;
    }

    public void concluir() {
        boolean temPendente = procedimentos.stream()
                .anyMatch(p -> p.getStatus() == StatusProcedimento.PENDENTE);
        
        if (temPendente) {
            throw new DomainException("Existem procedimentos pendentes no plano de tratamento");
        }
        this.status = StatusPlano.CONCLUIDO;
    }

    public void adicionarProcedimentoRetorno(Procedimento novoProcedimento) {
        this.historicoVersoes.add("Versão " + this.versaoAtual + " salva com sucesso");
        this.versaoAtual++;
        this.procedimentos.add(novoProcedimento);
    }

    public void validarExclusao() {
        boolean temRealizado = procedimentos.stream()
                .anyMatch(p -> p.getStatus() == StatusProcedimento.REALIZADO);
        
        if (temRealizado) {
            throw new DomainException("Planos com procedimentos realizados não podem ser excluídos");
        }
    }

    public Procedimento getProcedimentoPorDescricao(String descricao) {
        return procedimentos.stream()
                .filter(p -> p.getDescricao().equals(descricao))
                .findFirst()
                .orElseThrow(() -> new DomainException("Procedimento não encontrado: " + descricao));
    }
}