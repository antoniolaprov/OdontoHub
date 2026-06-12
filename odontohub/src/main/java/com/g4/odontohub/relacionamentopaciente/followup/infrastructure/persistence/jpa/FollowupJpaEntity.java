package com.g4.odontohub.relacionamentopaciente.followup.infrastructure.persistence.jpa;

import com.g4.odontohub.relacionamentopaciente.followup.domain.model.ChecklistFollowup;
import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupId;
import com.g4.odontohub.relacionamentopaciente.followup.domain.model.FollowupPosOperatorio;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "followup")
public class FollowupJpaEntity {

    @Id
    private Long id;
    private String paciente;
    private String tipoLigacao;
    private boolean concluida;

    // Checklist achatado (preenchido apenas quando a tarefa é concluída)
    private Boolean checkSangramento;
    private Integer checkNivelDor;
    private String checkObservacoes;
    private LocalDate checkDataLigacao;
    private String checkResponsavel;

    protected FollowupJpaEntity() {
    }

    public static FollowupJpaEntity fromDomain(FollowupPosOperatorio t) {
        FollowupJpaEntity e = new FollowupJpaEntity();
        e.id = t.getId().id();
        e.paciente = t.getPaciente();
        e.tipoLigacao = t.getTipoLigacao();
        e.concluida = t.isConcluida();
        ChecklistFollowup c = t.getChecklist();
        if (c != null) {
            e.checkSangramento = c.sangramentoAtivo();
            e.checkNivelDor = c.nivelDor();
            e.checkObservacoes = c.observacoes();
            e.checkDataLigacao = c.dataLigacao();
            e.checkResponsavel = c.responsavel();
        }
        return e;
    }

    public FollowupPosOperatorio toDomain() {
        ChecklistFollowup checklist = null;
        if (checkDataLigacao != null || checkResponsavel != null || checkNivelDor != null) {
            checklist = new ChecklistFollowup(
                    Boolean.TRUE.equals(checkSangramento),
                    checkNivelDor == null ? 0 : checkNivelDor,
                    checkObservacoes, checkDataLigacao, checkResponsavel);
        }
        return FollowupPosOperatorio.reconstituir(new FollowupId(id), paciente, tipoLigacao, concluida, checklist);
    }
}
