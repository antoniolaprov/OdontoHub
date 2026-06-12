package com.g4.odontohub.cadastropaciente.infrastructure.persistence.jpa;

import com.g4.odontohub.cadastropaciente.domain.model.AlteracaoCadastral;
import com.g4.odontohub.cadastropaciente.domain.model.Paciente;
import com.g4.odontohub.cadastropaciente.domain.model.PacienteRegistroId;
import com.g4.odontohub.cadastropaciente.domain.model.StatusPaciente;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "paciente")
public class PacienteJpaEntity {

    @Id
    private Long id;
    private String nomeCompleto;
    private String cpf;
    private String dataNascimento;
    private String telefone;
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusPaciente status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "paciente_alteracao", joinColumns = @JoinColumn(name = "paciente_id"))
    private List<AlteracaoCadastralEmbeddable> historico = new ArrayList<>();

    protected PacienteJpaEntity() {
    }

    public static PacienteJpaEntity fromDomain(Paciente p) {
        PacienteJpaEntity e = new PacienteJpaEntity();
        e.id = p.getId().id();
        e.nomeCompleto = p.getNomeCompleto();
        e.cpf = p.getCpf();
        e.dataNascimento = p.getDataNascimento();
        e.telefone = p.getTelefone();
        e.email = p.getEmail();
        e.status = p.getStatus();
        e.historico = p.getHistoricoAlteracoes().stream()
                .map(AlteracaoCadastralEmbeddable::fromDomain)
                .collect(Collectors.toList());
        return e;
    }

    public Paciente toDomain() {
        List<AlteracaoCadastral> hist = historico.stream()
                .map(AlteracaoCadastralEmbeddable::toDomain)
                .collect(Collectors.toList());
        return Paciente.reconstituir(new PacienteRegistroId(id), nomeCompleto, cpf, dataNascimento,
                telefone, email, status, hist);
    }
}
