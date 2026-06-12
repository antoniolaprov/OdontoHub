package com.g4.odontohub.estoque.infrastructure.persistence.jpa;

import com.g4.odontohub.estoque.domain.model.MaterialConsumivel;
import com.g4.odontohub.estoque.domain.model.MaterialId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "material_consumivel")
public class MaterialConsumivelJpaEntity {

    @Id
    private Long id;
    private String nome;
    private String unidadeMedida;
    private int quantidadeEmEstoque;
    private int pontoMinimo;
    private LocalDate ultimaAtualizacao;

    protected MaterialConsumivelJpaEntity() {
    }

    public static MaterialConsumivelJpaEntity fromDomain(MaterialConsumivel m) {
        MaterialConsumivelJpaEntity e = new MaterialConsumivelJpaEntity();
        e.id = m.getId().id();
        e.nome = m.getNome();
        e.unidadeMedida = m.getUnidadeMedida();
        e.quantidadeEmEstoque = m.getQuantidadeEmEstoque();
        e.pontoMinimo = m.getPontoMinimo();
        e.ultimaAtualizacao = m.getUltimaAtualizacao();
        return e;
    }

    public MaterialConsumivel toDomain() {
        return new MaterialConsumivel(new MaterialId(id), nome, unidadeMedida,
                quantidadeEmEstoque, pontoMinimo, ultimaAtualizacao);
    }
}
