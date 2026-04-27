package com.g4.odontohub.Pagamento.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Pagamento {
    
    private String id;
    private String pacienteId;
    private double valor;
    private String status;

    private Pagamento() {}

    public static Pagamento criar(String pacienteId, double valor) {
        if (pacienteId == null || pacienteId.trim().isEmpty()) {
            throw new DomainException("O ID do paciente é obrigatório para o pagamento.");
        }
        if (valor <= 0) {
            throw new DomainException("O valor do pagamento deve ser maior que zero.");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.id = UUID.randomUUID().toString();
        pagamento.pacienteId = pacienteId;
        pagamento.valor = valor;
        pagamento.status = "CONCLUIDO";
        
        return pagamento;
    }
}