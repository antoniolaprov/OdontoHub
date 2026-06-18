package com.g4.odontohub.equipe.infrastructure.persistence.jpa;

import com.g4.odontohub.equipe.domain.model.ModuloSistema;
import com.g4.odontohub.equipe.domain.model.RegistroAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

/** Mapeamento de persistência de um registro de auditoria do colaborador (Data Mapper). */
@Embeddable
public class AuditoriaEmbeddable {

    private LocalDateTime dataHora;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ModuloSistema modulo;
    @Column(length = 500)
    private String acao;

    protected AuditoriaEmbeddable() {
    }

    static AuditoriaEmbeddable fromDomain(RegistroAuditoria r) {
        AuditoriaEmbeddable e = new AuditoriaEmbeddable();
        e.dataHora = r.dataHora();
        e.modulo = r.modulo();
        e.acao = r.acao();
        return e;
    }

    RegistroAuditoria toDomain() {
        return new RegistroAuditoria(dataHora, modulo, acao);
    }
}
