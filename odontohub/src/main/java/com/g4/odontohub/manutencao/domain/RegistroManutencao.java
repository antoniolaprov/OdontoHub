package com.g4.odontohub.manutencao.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class RegistroManutencao {

    private UUID id;
    private UUID equipamentoId;
    private TipoManutencao tipo;
    private LocalDate dataInicio;
    private LocalDate dataConclusao;
    private String responsavelTecnico;
    private String descricao;
    private BigDecimal custo;

    private RegistroManutencao() {}

    public static RegistroManutencao criar(UUID equipamentoId, TipoManutencao tipo,
                                           LocalDate dataInicio, String responsavelTecnico,
                                           String descricao, BigDecimal custo) {
        if (responsavelTecnico == null || responsavelTecnico.isBlank()) {
            throw new DomainException("Responsável técnico é obrigatório no registro de manutenção");
        }
        if (tipo == null) {
            throw new DomainException("Tipo de manutenção é obrigatório");
        }
        if (dataInicio == null) {
            throw new DomainException("Data de início é obrigatória");
        }

        RegistroManutencao r = new RegistroManutencao();
        r.id = UUID.randomUUID();
        r.equipamentoId = equipamentoId;
        r.tipo = tipo;
        r.dataInicio = dataInicio;
        r.responsavelTecnico = responsavelTecnico;
        r.descricao = descricao;
        r.custo = custo;
        return r;
    }
}