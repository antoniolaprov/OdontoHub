package com.g4.odontohub.instrumental.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class CicloEsterilizacao {
    private Long id;
    private Long instrumentoId;
    private LocalDate data;
    private String responsavel;
    private MetodoEsterilizacao metodo;
    private LocalDate validade;

    private CicloEsterilizacao() {}

    public static CicloEsterilizacao criar(Long id, Long instrumentoId, MetodoEsterilizacao metodo, LocalDate data, String responsavel) {
        if (metodo == null) {
            throw new DomainException("Método de esterilização é obrigatório");
        }
        CicloEsterilizacao ciclo = new CicloEsterilizacao();
        ciclo.id = id;
        ciclo.instrumentoId = instrumentoId;
        ciclo.data = data;
        ciclo.responsavel = responsavel;
        ciclo.metodo = metodo;
        ciclo.validade = metodo.calcularValidade(data);
        return ciclo;
    }
}