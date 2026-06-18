package com.g4.odontohub.equipe.infrastructure.persistence.jpa;

import com.g4.odontohub.equipe.domain.model.AlteracaoCadastral;
import com.g4.odontohub.equipe.domain.model.Colaborador;
import com.g4.odontohub.equipe.domain.model.ColaboradorId;
import com.g4.odontohub.equipe.domain.model.DiaSemana;
import com.g4.odontohub.equipe.domain.model.Disponibilidade;
import com.g4.odontohub.equipe.domain.model.FuncaoColaborador;
import com.g4.odontohub.equipe.domain.model.PeriodoAusencia;
import com.g4.odontohub.equipe.domain.model.RegistroAuditoria;
import com.g4.odontohub.equipe.domain.model.StatusColaborador;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "colaborador")
public class ColaboradorJpaEntity {

    @Id
    private Long id;
    private String nomeCompleto;
    private String cpf;
    private String telefone;
    private String email;
    private String login;
    private String senha;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FuncaoColaborador funcao;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusColaborador status;
    private int tentativasLoginInvalidas;
    private boolean bloqueado;

    @Column(length = 80)
    private String disponibilidadeDias;
    private Integer disponibilidadeHoraInicio;
    private Integer disponibilidadeHoraFim;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "colaborador_ausencia", joinColumns = @JoinColumn(name = "colaborador_id"))
    private List<AusenciaEmbeddable> ausencias = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "colaborador_auditoria", joinColumns = @JoinColumn(name = "colaborador_id"))
    private List<AuditoriaEmbeddable> auditoria = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "colaborador_alteracao", joinColumns = @JoinColumn(name = "colaborador_id"))
    private List<AlteracaoEmbeddable> historicoAlteracoes = new ArrayList<>();

    private int atendimentos;
    private int faltas;
    private int conversoes;

    protected ColaboradorJpaEntity() {
    }

    public static ColaboradorJpaEntity fromDomain(Colaborador c) {
        ColaboradorJpaEntity e = new ColaboradorJpaEntity();
        e.id = c.getId().id();
        e.nomeCompleto = c.getNomeCompleto();
        e.cpf = c.getCpf();
        e.telefone = c.getTelefone();
        e.email = c.getEmail();
        e.login = c.getLogin();
        e.senha = c.getSenhaPersistencia();
        e.funcao = c.getFuncao();
        e.status = c.getStatus();
        e.tentativasLoginInvalidas = c.getTentativasLoginInvalidas();
        e.bloqueado = c.estaBloqueado();
        e.atendimentos = c.getAtendimentos();
        e.faltas = c.getFaltas();
        e.conversoes = c.getConversoes();

        Disponibilidade disp = c.getDisponibilidade();
        if (disp != null) {
            e.disponibilidadeDias = String.join(",", disp.getDiasDisponiveis().stream().map(Enum::name).toList());
            e.disponibilidadeHoraInicio = disp.getHoraInicio();
            e.disponibilidadeHoraFim = disp.getHoraFim();
            e.ausencias = new ArrayList<>();
            for (PeriodoAusencia a : disp.getAusencias()) {
                e.ausencias.add(AusenciaEmbeddable.fromDomain(a));
            }
        }
        e.auditoria = new ArrayList<>();
        for (RegistroAuditoria r : c.getAuditoria()) {
            e.auditoria.add(AuditoriaEmbeddable.fromDomain(r));
        }
        e.historicoAlteracoes = new ArrayList<>();
        for (AlteracaoCadastral a : c.getHistoricoAlteracoes()) {
            e.historicoAlteracoes.add(AlteracaoEmbeddable.fromDomain(a));
        }
        return e;
    }

    public Colaborador toDomain() {
        Disponibilidade disponibilidade = null;
        if (disponibilidadeDias != null && !disponibilidadeDias.isBlank()
                && disponibilidadeHoraInicio != null && disponibilidadeHoraFim != null) {
            Set<DiaSemana> dias = new LinkedHashSet<>();
            for (String dia : disponibilidadeDias.split(",")) {
                if (!dia.isBlank()) {
                    dias.add(DiaSemana.valueOf(dia.trim()));
                }
            }
            List<PeriodoAusencia> periodos = new ArrayList<>();
            for (AusenciaEmbeddable a : ausencias) {
                periodos.add(a.toDomain());
            }
            disponibilidade = new Disponibilidade(dias, disponibilidadeHoraInicio, disponibilidadeHoraFim, periodos);
        }
        List<RegistroAuditoria> registros = new ArrayList<>();
        for (AuditoriaEmbeddable a : auditoria) {
            registros.add(a.toDomain());
        }
        List<AlteracaoCadastral> alteracoes = new ArrayList<>();
        for (AlteracaoEmbeddable a : historicoAlteracoes) {
            alteracoes.add(a.toDomain());
        }
        return Colaborador.reconstituir(new ColaboradorId(id), nomeCompleto, cpf, telefone,
                email, login, senha, funcao, status, tentativasLoginInvalidas, bloqueado,
                disponibilidade, registros, alteracoes, atendimentos, faltas, conversoes);
    }
}
