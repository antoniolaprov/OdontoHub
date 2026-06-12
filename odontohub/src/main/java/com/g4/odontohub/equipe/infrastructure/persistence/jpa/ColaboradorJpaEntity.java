package com.g4.odontohub.equipe.infrastructure.persistence.jpa;

import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.model.ColaboradorId;
import com.g4.odontohub.equipe.domain.model.FuncaoColaborador;
import com.g4.odontohub.equipe.domain.model.StatusColaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "colaborador")
public class ColaboradorJpaEntity {

    @Id
    private Long id;
    private String nomeCompleto;
    private String cpf;
    private String telefone;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FuncaoColaborador funcao;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusColaborador status;

    protected ColaboradorJpaEntity() {
    }

    public static ColaboradorJpaEntity fromDomain(Colaborador c) {
        ColaboradorJpaEntity e = new ColaboradorJpaEntity();
        e.id = c.getId().id();
        e.nomeCompleto = c.getNomeCompleto();
        e.cpf = c.getCpf();
        e.telefone = c.getTelefone();
        e.funcao = c.getFuncao();
        e.status = c.getStatus();
        return e;
    }

    public Colaborador toDomain() {
        return Colaborador.reconstituir(new ColaboradorId(id), nomeCompleto, cpf, telefone, funcao, status);
    }
}
