package com.g4.odontohub.financeiro.infrastructure.persistence.jpa;

import com.g4.odontohub.financeiro.domain.model.Acordo;
import com.g4.odontohub.financeiro.domain.model.AcordoId;
import com.g4.odontohub.financeiro.domain.model.ParcelaCobranca;
import jakarta.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "acordo")
public class AcordoJpaEntity {

    @Id
    private Long id;
    private String paciente;
    private boolean inadimplido;
    private boolean multasRetroativasAtivas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "acordo_original", joinColumns = @JoinColumn(name = "acordo_id"))
    private List<ParcelaCobrancaEmbeddable> originais;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "acordo_nova", joinColumns = @JoinColumn(name = "acordo_id"))
    private List<ParcelaCobrancaEmbeddable> novas;

    protected AcordoJpaEntity() {
    }

    public static AcordoJpaEntity fromDomain(String paciente, Acordo a) {
        AcordoJpaEntity e = new AcordoJpaEntity();
        e.id = a.getId().id();
        e.paciente = paciente;
        e.inadimplido = a.isInadimplido();
        e.multasRetroativasAtivas = a.isMultasRetroativasAtivas();
        e.originais = a.getParcelasOriginais().stream()
                .map(ParcelaCobrancaEmbeddable::fromDomain).collect(Collectors.toList());
        e.novas = a.getNovasParcelas().stream()
                .map(ParcelaCobrancaEmbeddable::fromDomain).collect(Collectors.toList());
        return e;
    }

    public Acordo toDomain() {
        List<ParcelaCobranca> originaisDom = originais.stream()
                .map(ParcelaCobrancaEmbeddable::toDomain).collect(Collectors.toList());
        List<ParcelaCobranca> novasDom = novas.stream()
                .map(ParcelaCobrancaEmbeddable::toDomain).collect(Collectors.toList());
        return Acordo.reconstituir(new AcordoId(id), paciente, originaisDom, novasDom,
                inadimplido, multasRetroativasAtivas);
    }
}
