package com.g4.odontohub.manutencao.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
public class Equipamento {

    private UUID id;
    private String nome;
    private StatusEquipamento status;
    private Integer periodicidadeDias;
    private LocalDate proximaManutencao;
    private LocalDate ultimaManutencao;

    private Equipamento() {}

    public static Equipamento criar(String nome, StatusEquipamento status, Integer periodicidadeDias) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("Nome do equipamento é obrigatório");
        }
        Equipamento e = new Equipamento();
        e.id = UUID.randomUUID();
        e.nome = nome;
        e.status = status;
        e.periodicidadeDias = periodicidadeDias;
        return e;
    }

    public void iniciarManutencao() {
        this.status = StatusEquipamento.EM_MANUTENCAO;
    }

    public void verificarDisponibilidadeParaProcedimento() {
        if (this.status == StatusEquipamento.EM_MANUTENCAO) {
            throw new DomainException("Equipamento indisponível: em manutenção");
        }
    }

    public void concluirManutencao(LocalDate dataConclusao) {
        this.status = StatusEquipamento.DISPONIVEL;
        this.ultimaManutencao = dataConclusao;
        if (this.periodicidadeDias != null) {
            this.proximaManutencao = dataConclusao.plusDays(this.periodicidadeDias);
        }
    }

    public void definirPeriodicidade(Integer dias) {
        if (dias == null || dias <= 0) {
            throw new DomainException("Periodicidade deve ser maior que zero");
        }
        this.periodicidadeDias = dias;
        if (this.ultimaManutencao != null) {
            this.proximaManutencao = this.ultimaManutencao.plusDays(dias);
        }
    }

    public boolean alertaManutencaoProxima(LocalDate dataVerificacao, int diasAntecedencia) {
        if (this.proximaManutencao == null) return false;
        long diasRestantes = ChronoUnit.DAYS.between(dataVerificacao, this.proximaManutencao);
        return diasRestantes >= 0 && diasRestantes <= diasAntecedencia;
    }

    public long diasRestantesManutencao(LocalDate dataVerificacao) {
        if (this.proximaManutencao == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(dataVerificacao, this.proximaManutencao);
    }

}