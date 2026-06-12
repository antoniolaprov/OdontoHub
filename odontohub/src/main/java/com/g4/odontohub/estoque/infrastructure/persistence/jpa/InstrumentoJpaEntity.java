package com.g4.odontohub.estoque.infrastructure.persistence.jpa;

import com.g4.odontohub.estoque.domain.model.Instrumento;
import com.g4.odontohub.estoque.domain.model.InstrumentoId;
import com.g4.odontohub.estoque.domain.model.StatusEsterilizacao;
import com.g4.odontohub.estoque.domain.model.StatusInstrumento;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "instrumento")
public class InstrumentoJpaEntity {

    @Id
    private Long id;
    private String nome;
    private String categoria;
    private String codigoIdentificador;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusInstrumento statusInstrumento;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusEsterilizacao status;
    private LocalDate dataUltimaEsterilizacao;
    private LocalDate dataVencimento;
    private int prazoValidadeDias;
    private String responsavelEsterilizacao;
    private String tipo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "instrumento_componente", joinColumns = @JoinColumn(name = "instrumento_id"))
    @Column(name = "codigo_componente")
    private List<String> codigosComponentes = new ArrayList<>();

    protected InstrumentoJpaEntity() {
    }

    public static InstrumentoJpaEntity fromDomain(Instrumento i) {
        InstrumentoJpaEntity e = new InstrumentoJpaEntity();
        e.id = i.getId().id();
        e.nome = i.getNome();
        e.categoria = i.getCategoria();
        e.codigoIdentificador = i.getCodigoIdentificador();
        e.statusInstrumento = i.getStatusInstrumento();
        e.status = i.getStatus();
        e.dataUltimaEsterilizacao = i.getDataUltimaEsterilizacao();
        e.dataVencimento = i.getDataVencimento();
        e.prazoValidadeDias = i.getPrazoValidadeDias();
        e.responsavelEsterilizacao = i.getResponsavelEsterilizacao();
        e.tipo = i.getTipo();
        e.codigosComponentes = new ArrayList<>(i.getCodigosComponentes());
        return e;
    }

    public Instrumento toDomain() {
        return Instrumento.reconstituir(new InstrumentoId(id), nome, categoria, codigoIdentificador,
                statusInstrumento, status, dataUltimaEsterilizacao, dataVencimento, prazoValidadeDias,
                responsavelEsterilizacao, tipo, codigosComponentes);
    }
}
