package com.g4.odontohub.clinica.infrastructure.persistence.jpa;

import com.g4.odontohub.clinica.domain.model.Clinica;
import com.g4.odontohub.clinica.domain.model.ClinicaId;
import com.g4.odontohub.clinica.domain.model.StatusClinica;
import jakarta.persistence.*;

@Entity
@Table(name = "clinica")
public class ClinicaJpaEntity {

    @Id
    private Long id;
    private String nome;

    @Column(unique = true)
    private String cnpj;

    @Column(unique = true)
    private String email;

    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusClinica status;

    protected ClinicaJpaEntity() {
    }

    public static ClinicaJpaEntity fromDomain(Clinica c) {
        ClinicaJpaEntity e = new ClinicaJpaEntity();
        e.id = c.getId().id();
        e.nome = c.getNome();
        e.cnpj = c.getCnpj();
        e.email = c.getEmail();
        e.senhaHash = c.getSenhaHash();
        e.status = c.getStatus();
        return e;
    }

    public Clinica toDomain() {
        return Clinica.reconstituir(new ClinicaId(id), nome, cnpj, email, senhaHash, status);
    }
}
