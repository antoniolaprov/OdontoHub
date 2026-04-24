package com.g4.odontohub.instrumental.domain;

import java.time.LocalDate;

public enum MetodoEsterilizacao {
    AUTOCLAVE {
        @Override
        public LocalDate calcularValidade(LocalDate dataEsterilizacao) {
            return dataEsterilizacao.plusMonths(6);
        }
    },
    ESTUFA {
        @Override
        public LocalDate calcularValidade(LocalDate dataEsterilizacao) {
            return dataEsterilizacao.plusMonths(3);
        }
    },
    GLUTARALDEIDO {
        @Override
        public LocalDate calcularValidade(LocalDate dataEsterilizacao) {
            return dataEsterilizacao.plusDays(14);
        }
    };

    public abstract LocalDate calcularValidade(LocalDate dataEsterilizacao);
}