package com.g4.odontohub.material.domain;

import com.g4.odontohub.shared.exception.DomainException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Material {
    
    private String id;
    private String nome;
    private int quantidadeEmEstoque;

    private Material() {}

    public static Material criar(String nome, int quantidadeInicial) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("O nome do material é obrigatório.");
        }
        if (quantidadeInicial < 0) {
            throw new DomainException("A quantidade inicial não pode ser negativa.");
        }

        Material material = new Material();
        material.id = UUID.randomUUID().toString();
        material.nome = nome;
        material.quantidadeEmEstoque = quantidadeInicial;
        
        return material;
    }

    public void repor(int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("A quantidade para reposição deve ser maior que zero.");
        }
        this.quantidadeEmEstoque += quantidade;
    }
}